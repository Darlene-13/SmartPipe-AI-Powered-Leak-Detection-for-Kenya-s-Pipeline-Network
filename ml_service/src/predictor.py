import numpy as np
import logging
from collections import deque
from queue import Queue, Empty
from concurrent.futures import Future
from threading import RLock, Thread
from models import registry
from preprocessor import preprocess


class PipelinePredictor:
    def __init__(self):
        self._buffers = {}
        self._buffer_locks = {}
        self._registry_lock = RLock()
        logging.info("Pipeline Predictor initialized")

        self._batch_queue = Queue()  # (X, device_id, Future)
        self._batch_size = 16
        self._batch_timeout = 0.01

        # Self background batch worker
        self._worker = Thread(
            target=self._batch_worker,
            name = "BatchInferenceWorker",
            daemon = True
        )

        self._worker.start()
        logging.info("Pipeline Predictor started.......batch worker running")


    def _get_device_lock(self, device_id:str) -> RLock:
        with self._registry_lock:
            if device_id in self._buffer_locks:
                self._buffer_locks[device_id] = RLock()
                self._buffers[device_id] = deque(
                    maxlen = registry.window_size
                )
                logging.info(f"New buffer + lock created for device: {device_id}")
            return self._buffer_locks[device_id]


    def _get_buffer(self, device_id:str) -> deque:
        with self._registry_lock:
            return self._buffers[device_id]


    # Background batch worker

    def _batch_worker(self):

        while True:
            batch_X = []
            batch_futures =[]
            batch_ids = []

            # Wait for atleast one item
            try:
                X, device_id, fut = self._batch_queue.get(timeout=1.0)
                batch_X.append(X)
                batch_futures.append(fut)
                batch_ids.append(device_id)
            except Empty:
                continue

            # Collect more items
            while len(batch_X) < self.batch_size:
                try:
                    X, device_id, fut = self._batch_queue.get(timeout=self._batch_timeout)
                    batch_X.append(X)
                    batch_futures.append(fut)
                    batch_ids.append(device_id)

                except Empty:
                    break

                # Single batched inference call
                try:
                    batch_array = np.vstack(batch_X)
                    logging.info(
                        f"Batch inference: {len(batch_X)} requests"
                        f"| shape: {batch_array.shape}"
                    )

                    # One model calls for all awaiting requests
                    all_probas = registry.predict_proba(batch_array)

                    # Distribute results back to each column
                    for i, (fut, device_id) in enumerate(zip(batch_futures, batch_ids)):
                        if not fut.cancelled():
                            fut.set_result((all_probas[i], device_id))
                except Exception as e:
                    logging.error(f"Batch inference failed: {e}")

                    for fut in batch_futures:
                        if not fut.cancelled() and not fut.done():
                            fut.set_exception(e)


    def predict(self, device_id:str, raw:dict) -> dict:
        scaled = preprocess(raw)

        device_lock = self._get_device_lock(device_id)

        with device_lock:
            buffer = self._get_buffer(device_id)
            buffer.append(scaled[0])
            buffer_len = len(buffer)

            # check if window is full
            if buffer_len < registry.window_size:
                return {
                    "status" : "Collecting.......?",
                    "prediction" : None,
                    "window_progress": f"{buffer_len}/{registry.window_size}",
                    "message": f"Need {registry.window_size - buffer_len} more readings"
                }
            sequence = np.array(list(buffer))

        X = sequence.reshape(
            1, registry.window_size, registry.n_features
        )

        fut = Future()
        self._batch_queue.put((X, device_id, fut))

        try:
            proba, _= fut.result(timeout=30.0)
        except TimeoutError:
            logging.error(f"Inference timeout for device: {device_id}")
            return {"status": "error", "message": "Inference timeout"}
        except Exception as e:
            logging.error(f"Inference error for device: {device_id}: {e}")
            return {"status": "error", "message": str(e)}

        result = registry.decode_prediction(proba)
        result["status"]          = "predicted"
        result["window_progress"] = f"{registry.window_size}/{registry.window_size}"
        result["device_id"]       = device_id

        logging.info(
            f"Device {device_id} → {result['label']} "
            f"({result['confidence'] * 100:.1f}%)"
        )
        return result


    def reset_buffer(self, device_id: str) -> dict:
        device_lock = self._get_device_lock(device_id)
        with device_lock:
            self._get_buffer(device_id).clear()
            logging.info(f"Device {device_id} buffer reset")

    # Status snapshot
    def get_buffer_status(self)-> dict:
        with self._registry_lock:
            device_ids = list(self._buffers.keys())

        status = {}
        for device_id in device_ids:
            lock = self._get_device_lock(device_id)

            if lock:
                with lock:
                    buf = self._buffers.get(device_id)
                    if buf is not None:
                        status[device_id] = len(buf)
            return status


predictor = PipelinePredictor()




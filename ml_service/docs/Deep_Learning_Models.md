# LSTM (LONG SHORT TERM MEMORY)

- It is a special kind of Recurrent neural network designed for sequences.
- It uses a memory cell to store information over time overcoming short term memory limitations like traditional RNNs
- It remembers past information using cells and gates

## f_t = sigmoid(W_f · [h_(t-1), x_t] + b_f)
Where:
- Sigmoid is the recurrent network activation function
- W_f is the weight matrix (Kernel) that is associated with the forget gate, it is the thing the network learns during training.
- Every single number starts at some initial value and the gradient descents slowly adjusting every single number until the network is good at detecting leaks.
- [h_(t-1), x_t] denotes the concatenation of the inputs and the previous hidden state.
- b_f is the bias with the forget gate.


- LSTM has four gates: Forget, Input, Candidate,Output.
- Each gate has two matrices: One for the input (x_t) called the kernel and one for the previous hidden state h_(t-1)

```
4 gates × 2 matrices = 8 weight matrices total

Keras groups them as:
  kernel           → shape (n_features, 4 × n_units)
  recurrent_kernel → shape (n_units,    4 × n_units)
  bias             → shape (4 × n_units,)
```


- When you see kernel_initializer in Keras docs, it's talking about the input-side weight matrix.
- When you see recurrent_initializer, it's the hidden-state-side weight matrix.
- They are initialized differently because they do different things — which leads us to the next part.
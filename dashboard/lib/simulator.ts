// Aligned to Java backend enums: SeverityLevel + FaultClass

// Maps to Java: FaultClass enum
export type FaultClass = "NORMAL" | "LEAK" | "BLOCKAGE"

// Maps to Java: SeverityLevel enum (used for SystemStatus)
export type SystemStatusLevel = "NORMAL" | "LEAK_DETECTED" | "BLOCKAGE_DETECTED" | "OFFLINE"

export type Severity = "none" | "incipient" | "moderate" | "critical"
export type NodeId = "A" | "B" | "C"

// FaultClass descriptions from Java enum
export const FAULT_CLASS_DESC: Record<FaultClass, string> = {
  NORMAL: "Pipeline operating within the normal parameters",
  LEAK: "Abrasive leak signature detected in pressure profile",
  BLOCKAGE: "Partial blockage detected - flow restriction present",
}

// SystemStatus descriptions from Java enum
export const SYSTEM_STATUS_DESC: Record<SystemStatusLevel, { description: string; color: string; requiresAction: boolean }> = {
  NORMAL: { description: "Pipeline Operating Normally", color: "#00FF00", requiresAction: false },
  LEAK_DETECTED: { description: "Leak detected - operator action required", color: "#FF0000", requiresAction: true },
  BLOCKAGE_DETECTED: { description: "Blockage detected - operator action required", color: "#FFA500", requiresAction: true },
  OFFLINE: { description: "System offline - no data received", color: "#808080", requiresAction: true },
}

export interface SensorReading {
  nodeId: NodeId
  pressure: number
  timestamp: string
  flowRate: number
  dpdt: number
}

export interface FaultDetection {
  classification: FaultClass
  confidence: number
  dpdt: number
  affectedNode: NodeId | null
  severity: Severity
  description: string
}

export interface Alert {
  id: string
  timestamp: string
  type: FaultClass
  severity: Severity
  confidence: number
  nodeId: NodeId | null
  message: string
}

export interface SystemStatus {
  level: SystemStatusLevel
  description: string
  requiresAction: boolean
  flowRate: number
  dpdt: number
  uptime: number
  pumpRpm: number
  slurryDensity: number
}

export interface AIRecommendation {
  urgent: boolean
  message: string
  actions: string[]
}

export interface LatencyStats {
  lastInjected: string | null
  alertReceived: string | null
  endToEndMs: number | null
  averageMs: number
  maxMs: number
  allWithinTarget: boolean
}

// Baseline pressures for each node (Pa)
const BASELINE: Record<NodeId, number> = { A: 245000, B: 198000, C: 180000 }

// Current scenario state
let currentScenario: FaultClass = "NORMAL"
let currentSeverity: Severity = "none"
let faultStartTime: number | null = null
let faultNode: NodeId = "B"

// Latency tracking
const latencyHistory: number[] = []
let lastInjectionTime: string | null = null
let lastAlertTime: string | null = null

export function setScenario(scenario: FaultClass, severity: Severity = "critical", node: NodeId = "B") {
  currentScenario = scenario
  currentSeverity = severity
  faultNode = node
  faultStartTime = Date.now()

  if (scenario !== "NORMAL") {
    lastInjectionTime = new Date().toISOString()
    const latency = 800 + Math.random() * 1700
    setTimeout(() => {
      lastAlertTime = new Date().toISOString()
      latencyHistory.push(latency)
    }, latency)
  } else {
    faultStartTime = null
  }
}

export function getScenario() {
  return { scenario: currentScenario, severity: currentSeverity, faultNode }
}

function noise(scale: number) {
  return (Math.random() - 0.5) * 2 * scale
}

export function generateReadings(): SensorReading[] {
  const now = new Date().toISOString()
  const nodes: NodeId[] = ["A", "B", "C"]

  return nodes.map((nodeId) => {
    let pressure = BASELINE[nodeId] + noise(2000)
    let flowRate = 2.1 + noise(0.15)
    let dpdt = noise(15)

    if (currentScenario === "LEAK" && faultStartTime) {
      const elapsed = (Date.now() - faultStartTime) / 1000
      const sf = currentSeverity === "critical" ? 1 : currentSeverity === "moderate" ? 0.5 : 0.2

      if (nodeId === faultNode) {
        const drop = Math.min(elapsed * 450 * sf, BASELINE[nodeId] * 0.6)
        pressure = BASELINE[nodeId] - drop + noise(1000)
        dpdt = -450 * sf + noise(20)
        flowRate = 2.1 - elapsed * 0.1 * sf + noise(0.05)
      } else {
        pressure -= elapsed * 50 * sf + noise(500)
        dpdt = -50 * sf + noise(10)
      }
    } else if (currentScenario === "BLOCKAGE" && faultStartTime) {
      const elapsed = (Date.now() - faultStartTime) / 1000
      const sf = currentSeverity === "critical" ? 1 : currentSeverity === "moderate" ? 0.5 : 0.2

      if (nodeId === faultNode) {
        pressure = BASELINE[nodeId] + elapsed * 200 * sf + noise(1000)
        dpdt = 200 * sf + noise(20)
        flowRate = Math.max(0.1, 2.1 - elapsed * 0.15 * sf) + noise(0.05)
      }
    }

    return {
      nodeId,
      pressure: Math.max(0, Math.round(pressure)),
      timestamp: now,
      flowRate: Math.max(0, parseFloat(flowRate.toFixed(2))),
      dpdt: parseFloat(dpdt.toFixed(1)),
    }
  })
}

export function getFaultDetection(readings: SensorReading[]): FaultDetection {
  if (currentScenario === "NORMAL") {
    return {
      classification: "NORMAL",
      confidence: 98 + Math.random() * 1.5,
      dpdt: readings.reduce((sum, r) => sum + r.dpdt, 0) / readings.length,
      affectedNode: null,
      severity: "none",
      description: FAULT_CLASS_DESC.NORMAL,
    }
  }

  const faultReading = readings.find((r) => r.nodeId === faultNode)
  return {
    classification: currentScenario,
    confidence: 94 + Math.random() * 5,
    dpdt: faultReading?.dpdt ?? 0,
    affectedNode: faultNode,
    severity: currentSeverity,
    description: FAULT_CLASS_DESC[currentScenario],
  }
}

export function getSystemStatus(readings: SensorReading[]): SystemStatus {
  const avgFlow = readings.reduce((s, r) => s + r.flowRate, 0) / readings.length
  const avgDpdt = readings.reduce((s, r) => s + r.dpdt, 0) / readings.length

  let level: SystemStatusLevel = "NORMAL"
  if (currentScenario === "LEAK") level = "LEAK_DETECTED"
  else if (currentScenario === "BLOCKAGE") level = "BLOCKAGE_DETECTED"

  const info = SYSTEM_STATUS_DESC[level]

  return {
    level,
    description: info.description,
    requiresAction: info.requiresAction,
    flowRate: parseFloat(avgFlow.toFixed(2)),
    dpdt: parseFloat(avgDpdt.toFixed(1)),
    uptime: 99.7 + Math.random() * 0.3,
    pumpRpm: Math.round(1450 + noise(15)),
    slurryDensity: parseFloat((1.65 + noise(0.02)).toFixed(3)),
  }
}

export function getAIRecommendation(fault: FaultDetection): AIRecommendation {
  if (fault.classification === "NORMAL") {
    return {
      urgent: false,
      message: "System operating within normal parameters. No action required. Next scheduled inspection due in 48 hours.",
      actions: [],
    }
  }

  if (fault.classification === "LEAK") {
    return {
      urgent: true,
      message: `ABRASIVE LEAK detected at Node ${fault.affectedNode}. Pressure drop rate: ${fault.dpdt.toFixed(0)} Pa/s. ${fault.description}. Immediate action required.`,
      actions: [
        "Isolate pump discharge valve",
        "Notify shift supervisor immediately",
        "Inspect pipeline at affected node mark",
        "Do not restart pump until inspection complete",
      ],
    }
  }

  return {
    urgent: true,
    message: `BLOCKAGE detected at Node ${fault.affectedNode}. Pressure building at ${fault.dpdt.toFixed(0)} Pa/s. ${fault.description}.`,
    actions: [
      "Reduce pump speed to 50%",
      "Open bypass valve if available",
      "Schedule pipe inspection within 2 hours",
      "Monitor for pressure relief valve activation",
    ],
  }
}

let alertIdCounter = 0

export function generateAlert(fault: FaultDetection): Alert {
  alertIdCounter++
  return {
    id: `ALT-${alertIdCounter.toString().padStart(4, "0")}`,
    timestamp: new Date().toISOString(),
    type: fault.classification,
    severity: fault.severity,
    confidence: parseFloat(fault.confidence.toFixed(1)),
    nodeId: fault.affectedNode,
    message:
      fault.classification === "NORMAL"
        ? "System nominal"
        : `${fault.classification} detected at Node ${fault.affectedNode}`,
  }
}

export function getLatencyStats(): LatencyStats {
  const avg = latencyHistory.length > 0 ? latencyHistory.reduce((a, b) => a + b, 0) / latencyHistory.length : 0
  const max = latencyHistory.length > 0 ? Math.max(...latencyHistory) : 0

  return {
    lastInjected: lastInjectionTime,
    alertReceived: lastAlertTime,
    endToEndMs: latencyHistory.length > 0 ? latencyHistory[latencyHistory.length - 1] : null,
    averageMs: Math.round(avg),
    maxMs: Math.round(max),
    allWithinTarget: max < 5000,
  }
}

// Generate historical data
export function generateHistoricalData(fromDate: Date, toDate: Date) {
  const readings: Array<SensorReading & { id: number }> = []
  const alerts: Alert[] = []
  let id = 0
  const faultCounts = { NORMAL: 0, LEAK: 0, BLOCKAGE: 0 }

  const current = new Date(fromDate)
  while (current <= toDate) {
    const isAnomaly = Math.random() < 0.04
    const faultType: FaultClass = isAnomaly ? (Math.random() < 0.6 ? "LEAK" : "BLOCKAGE") : "NORMAL"
    faultCounts[faultType]++

    const nodes: NodeId[] = ["A", "B", "C"]
    for (const nodeId of nodes) {
      id++
      let pressure = BASELINE[nodeId] + noise(3000)
      if (faultType === "LEAK" && nodeId === "B") {
        pressure -= 50000 + Math.random() * 50000
      } else if (faultType === "BLOCKAGE" && nodeId === "B") {
        pressure += 30000 + Math.random() * 20000
      }

      readings.push({
        id,
        nodeId,
        pressure: Math.max(0, Math.round(pressure)),
        timestamp: current.toISOString(),
        flowRate: parseFloat((2.1 + noise(0.3)).toFixed(2)),
        dpdt: parseFloat(noise(faultType === "NORMAL" ? 15 : 300).toFixed(1)),
      })
    }

    if (isAnomaly) {
      alertIdCounter++
      alerts.push({
        id: `ALT-${alertIdCounter.toString().padStart(4, "0")}`,
        timestamp: current.toISOString(),
        type: faultType,
        severity: Math.random() < 0.3 ? "critical" : Math.random() < 0.5 ? "moderate" : "incipient",
        confidence: parseFloat((90 + Math.random() * 9).toFixed(1)),
        nodeId: "B",
        message: `${faultType} detected at Node B`,
      })
    }

    current.setTime(current.getTime() + 15 * 60 * 1000)
  }

  return { readings, alerts, faultCounts }
}

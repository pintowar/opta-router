import axios from "axios";
import { components } from "./generated/api";
type RouteInstance = components["schemas"]["RouteInstance"];
type VrpSolution = components["schemas"]["VrpSolution"];
type VrpSolutionRegistry = components["schemas"]["VrpSolutionRegistry"];
type PanelSolutionState = components["schemas"]["PanelSolutionState"];
type SolverState = "NOT_SOLVED" | "RUNNING" | "TERMINATED";

const defaultHeaders = {
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

async function getInstances(): Promise<RouteInstance[]> {
  const { data, status } = await axios.get<VrpSolution[]>("/api/solutions");

  return status === 200 ? data.map((it) => it.instance) : Promise.reject("Failed to retrieve instances");
}

async function getSolution(id: number): Promise<VrpSolution | null> {
  try {
    const { data, status } = await axios.get<VrpSolution>(`/api/solutions/by-instance-id/${id}/show`);

    return status === 200 ? data : Promise.reject("Failed to retrieve solution");
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function solve(id: number): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.post<SolverState>(`/api/solver/${id}/solve`, defaultHeaders);
    return status === 200 ? data : Promise.reject(`Failed to solve instance ${id}`);
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function getPanelSolutionState(id: number): Promise<PanelSolutionState | null> {
  try {
    const { data, status } = await axios.get<PanelSolutionState>(`/api/solver/${id}/solution-state`, defaultHeaders);

    return status === 200 ? data : Promise.reject("Failed to retrieve solver solution/state");
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function detailedPath(id: number, detailed: boolean): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.put<SolverState>(
      `/api/solver/${id}/detailed-path/${detailed}`,
      defaultHeaders
    );

    return status === 200 ? data : Promise.reject("Failed to change detailed-path option");
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function terminate(id: number): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.get<SolverState>(`/api/solver/${id}/terminate`, defaultHeaders);
    return status === 200 ? data : Promise.reject("Failed to terminate solver");
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function clean(id: number): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.get<SolverState>(`/api/solver/${id}/clean`, defaultHeaders);
    return status === 200 ? data : Promise.reject("Failed to clear actual solution");
  } catch (e) {
    return Promise.resolve(null);
  }
}

export type { RouteInstance, SolverState, VrpSolution, VrpSolutionRegistry };

export { getInstances, getSolution, solve, terminate, clean, detailedPath, getPanelSolutionState };

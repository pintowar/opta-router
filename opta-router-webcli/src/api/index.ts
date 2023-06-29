import axios from "axios";
import { components } from "./generated/api";
type Route = components["schemas"]["Route"];
type Vehicle = components["schemas"]["Vehicle"];
type VrpProblem = components["schemas"]["VrpProblem"];
type VrpSolution = components["schemas"]["VrpSolution"];
type PanelSolutionState = components["schemas"]["PanelSolutionState"];
type SolverState = "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";

const defaultHeaders = {
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

async function getProblems(): Promise<VrpProblem[]> {
  const { data, status } = await axios.get<VrpProblem[]>("/api/vrp-problems");

  return status === 200 ? data : Promise.reject("Failed to retrieve instances");
}

async function getProblem(id: number): Promise<VrpProblem | null> {
  try {
    const { data, status } = await axios.get<VrpProblem>(`/api/vrp-problems/${id}`);

    return status === 200 ? data : Promise.reject("Failed to retrieve instance");
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function getSolverNames(): Promise<string[]> {
  try {
    const { data, status } = await axios.get<string[]>(`/api/solver/solver-names`, defaultHeaders);

    return status === 200 ? data : Promise.reject("Failed to retrieve solver solution/state");
  } catch (e) {
    return Promise.resolve([]);
  }
}

async function solve(id: number, solverName: string): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.post<SolverState>(`/api/solver/${id}/solve/${solverName}`, defaultHeaders);
    return status === 200 ? data : Promise.reject(`Failed to solve instance ${id}`);
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function getPanelSolutionState(id: number): Promise<PanelSolutionState | null> {
  try {
    const { data, status } = await axios.get<PanelSolutionState>(`/api/solver/${id}/solution-panel`, defaultHeaders);

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

export type { Route, SolverState, Vehicle, VrpProblem, VrpSolution };

export { getProblems, getProblem, getSolverNames, solve, terminate, clean, detailedPath, getPanelSolutionState };

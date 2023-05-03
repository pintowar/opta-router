import axios from "axios";
import { components } from "./generated/api";
type Instance = components["schemas"]["Instance"];
type SolverState = components["schemas"]["SolverState"];
type VrpSolution = components["schemas"]["VrpSolution"];
type VrpSolutionState = components["schemas"]["VrpSolutionState"];

const defaultHeaders = {
  headers: {
    "Content-Type": "application/json",
    Accept: "application/json",
  },
};

async function getInstances(): Promise<Instance[]> {
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

async function solve(instance: Instance): Promise<SolverState | null> {
  try {
    const { data, status } = await axios.post<SolverState>(
      `/api/solver/${instance.id}/solve`,
      instance,
      defaultHeaders
    );
    return status === 200 ? data : Promise.reject(`Failed to solve instance ${instance.id}`);
  } catch (e) {
    return Promise.resolve(null);
  }
}

async function getSolutionState(id: number): Promise<VrpSolutionState | null> {
  try {
    const { data, status } = await axios.get<VrpSolutionState>(`/api/solver/${id}/solution-state`, defaultHeaders);

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

export type { Instance, SolverState, VrpSolution, VrpSolutionState };

export { getInstances, getSolution, solve, terminate, clean, detailedPath, getSolutionState };

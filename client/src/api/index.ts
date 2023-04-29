import axios, { AxiosError } from 'axios';
import { components } from "./generated/api";
type Instance = components["schemas"]["Instance"];
type Status = components["schemas"]["SolverState"];
type VrpSolution = components["schemas"]["VrpSolution"];

const defaultHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
};

async function getInstances(): Promise<Instance[]> {
    const { data, status } = await axios.get<Instance[]>(
        "/api/instances"
    )

    return status === 200 ? data : Promise.reject("Failed to retrieve instances");
}

async function getInstance(id: number): Promise<Instance | null> {
    try {
        const { data, status } = await axios.get<Instance>(
            `/api/instances/${id}/show`
        )
    
        return status === 200 ? data : Promise.reject("Failed to retrieve instance");
    } catch (e) {
        return Promise.resolve(null);
    }
    
}

async function solve(instance: Instance): Promise<Status> {
    const { data, status } = await axios.post<Status>(
        `/api/solver/${instance.id}/solve`, 
        instance,
        defaultHeaders
    )
    return status === 200 ? data : Promise.reject(`Failed to solve instance ${instance.id}`);
}

// async function status(): Promise<Status> {
//     const { data, status } = await axios.get<Status>(
//         "/api/status", 
//         defaultHeaders
//     )

//     return status === 200 ? data : Promise.reject("Failed to retrieve solver status");
// }

// async function instance(): Promise<Instance> {
//     const { data, status } = await axios.get<Instance>(
//         "/api/instance", 
//         defaultHeaders
//     )

//     return status === 200 ? data : Promise.reject("Failed to retrieve solving instance");
// }

// async function solution(): Promise<VrpSolution> {
//     const { data, status } = await axios.get<VrpSolution>(
//         "/api/solution", 
//         defaultHeaders
//     )

//     return status === 200 ? data : Promise.reject("Failed to retrieve current solution");
// }

async function detailedPath(id: number, detailed: boolean): Promise<Status> {
    const { data, status } = await axios.put<Status>(
        `/api/solver/${id}/detailed-path/${detailed}`, 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to change detailed-path option");
}

async function terminate(id: number,): Promise<Status> {
    const { data, status } = await axios.get<Status>(
        `/api/solver/${id}/terminate`, 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to terminate solver");
}

async function destroy(id: number,): Promise<Status> {
    const { data, status } = await axios.get<Status>(
        `/api/solver/${id}/clean`, 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to destroy actual solution");
}

export type { Instance, Status, VrpSolution }

export { getInstances, getInstance, solve, terminate, destroy, status, detailedPath }
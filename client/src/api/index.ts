import axios from 'axios';
import { components } from "./generated/api";
type Instance = components["schemas"]["Instance"];
type Status = components["schemas"]["Status"];
type VrpSolution = components["schemas"]["VrpSolution"];

const defaultHeaders = {
    headers: {
      'Content-Type': 'application/json',
      Accept: 'application/json',
    },
};

async function getSessionId(): Promise<string> {
    const { data, status } = await axios.get<string>(
        "/api/session-id"
    )

    return status === 200 ? data : Promise.reject("Failed to retrieve session id");
}

async function solve(instance: Instance): Promise<Status> {
    const { data, status } = await axios.post<Status>(
        "/api/solve", 
        instance,
        defaultHeaders
    )
    return status === 200 ? data : Promise.reject(`Failed to solve instance ${instance.id}`);
}

async function status(): Promise<Status> {
    const { data, status } = await axios.get<Status>(
        "/api/status", 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to retrieve solver status");
}

async function instance(): Promise<Instance> {
    const { data, status } = await axios.get<Instance>(
        "/api/instance", 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to retrieve solving instance");
}

async function solution(): Promise<VrpSolution> {
    const { data, status } = await axios.get<VrpSolution>(
        "/api/solution", 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to retrieve current solution");
}

async function detailedPath(detailed: boolean): Promise<Status> {
    const { data, status } = await axios.put<Status>(
        `/api/detailed-path/${detailed}`, 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to change detailed-path option");
}

async function terminate(): Promise<Status> {
    const { data, status } = await axios.get<Status>(
        "/api/terminate", 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to terminate solver");
}

async function destroy(): Promise<Status> {
    const { data, status } = await axios.get<Status>(
        "/api/clean", 
        defaultHeaders
    )

    return status === 200 ? data : Promise.reject("Failed to destroy actual solution");
}

export type { Instance, Status, VrpSolution }

export { getSessionId, solve, terminate, destroy, status, detailedPath }
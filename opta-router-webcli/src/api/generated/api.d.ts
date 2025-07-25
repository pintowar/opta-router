/**
 * This file was auto-generated by openapi-typescript.
 * Do not make direct changes to the file.
 */

export interface paths {
  "/api/vrp-vehicles/{id}/update": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put: operations["update"];
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-problems/{id}/update": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put: operations["update_1"];
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-locations/{id}/update": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put: operations["update_2"];
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/{id}/detailed-path/{isDetailed}": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put: operations["detailedPath"];
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-vehicles/insert": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["insert"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-problems/{id}/copy": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["copy"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-problems": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["index"];
    put?: never;
    post: operations["create"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-locations/insert": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["insert_1"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/{id}/terminate": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["terminate"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/{id}/solve/{solverName}": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["solve"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/{id}/clean": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post: operations["clear"];
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-vehicles": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["index_1"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-vehicles/by-depots": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["listByDepot"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-problems/{id}": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["show"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-locations": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["index_2"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-locations/{kind}": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["list"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/{id}/solution-panel": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["solutionState"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver/solver-names": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["solverNames"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver-history/{problemId}/solutions": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["solutions"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/solver-history/{problemId}/requests/{solverName}": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get: operations["requests"];
    put?: never;
    post?: never;
    delete?: never;
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-vehicles/{id}/remove": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post?: never;
    delete: operations["remove"];
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-problems/{id}/remove": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post?: never;
    delete: operations["remove_1"];
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
  "/api/vrp-locations/{id}/remove": {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    get?: never;
    put?: never;
    post?: never;
    delete: operations["remove_2"];
    options?: never;
    head?: never;
    patch?: never;
    trace?: never;
  };
}
export type webhooks = Record<string, never>;
export interface components {
  schemas: {
    Depot: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
    };
    Vehicle: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: int32 */
      capacity: number;
      depot: components["schemas"]["Depot"];
    };
    Customer: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
      /** Format: int32 */
      demand: number;
    };
    VrpProblem: {
      /** Format: int64 */
      id: number;
      name: string;
      vehicles: components["schemas"]["Vehicle"][];
      customers: components["schemas"]["Customer"][];
    };
    LocationRequest: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
      /** Format: int32 */
      demand?: number;
    };
    PageVehicle: {
      /** Format: int64 */
      totalElements?: number;
      /** Format: int32 */
      totalPages?: number;
      first?: boolean;
      last?: boolean;
      /** Format: int32 */
      size?: number;
      content?: components["schemas"]["Vehicle"][];
      /** Format: int32 */
      number?: number;
      sort?: components["schemas"]["SortObject"];
      /** Format: int32 */
      numberOfElements?: number;
      pageable?: components["schemas"]["PageableObject"];
      empty?: boolean;
    };
    PageableObject: {
      /** Format: int64 */
      offset?: number;
      sort?: components["schemas"]["SortObject"];
      paged?: boolean;
      /** Format: int32 */
      pageNumber?: number;
      /** Format: int32 */
      pageSize?: number;
      unpaged?: boolean;
    };
    SortObject: {
      empty?: boolean;
      sorted?: boolean;
      unsorted?: boolean;
    };
    PageVrpProblemSummary: {
      /** Format: int64 */
      totalElements?: number;
      /** Format: int32 */
      totalPages?: number;
      first?: boolean;
      last?: boolean;
      /** Format: int32 */
      size?: number;
      content?: components["schemas"]["VrpProblemSummary"][];
      /** Format: int32 */
      number?: number;
      sort?: components["schemas"]["SortObject"];
      /** Format: int32 */
      numberOfElements?: number;
      pageable?: components["schemas"]["PageableObject"];
      empty?: boolean;
    };
    VrpProblemSummary: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: int32 */
      nLocations?: number;
      /** Format: int32 */
      nVehicles?: number;
      /** Format: int32 */
      totalCapacity: number;
      /** Format: int32 */
      totalDemand: number;
      /** Format: int32 */
      numEnqueuedRequests: number;
      /** Format: int32 */
      numRunningRequests: number;
      /** Format: int32 */
      numTerminatedRequests: number;
      /** Format: int32 */
      numNotSolvedRequests: number;
      /** Format: int32 */
      numSolverRequests: number;
      /** Format: int32 */
      nlocations: number;
      /** Format: int32 */
      nvehicles: number;
    };
    Location: {
      name: string;
      /** Format: int64 */
      id: number;
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
    };
    PageLocation: {
      /** Format: int64 */
      totalElements?: number;
      /** Format: int32 */
      totalPages?: number;
      first?: boolean;
      last?: boolean;
      /** Format: int32 */
      size?: number;
      content?: components["schemas"]["Location"][];
      /** Format: int32 */
      number?: number;
      sort?: components["schemas"]["SortObject"];
      /** Format: int32 */
      numberOfElements?: number;
      pageable?: components["schemas"]["PageableObject"];
      empty?: boolean;
    };
    LatLng: {
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
    };
    PanelSolutionState: {
      solverPanel: components["schemas"]["SolverPanel"];
      solutionState: components["schemas"]["VrpSolutionRequest"];
    };
    Route: {
      distance: number;
      time: number;
      /** Format: int32 */
      totalDemand: number;
      order: components["schemas"]["LatLng"][];
      customerIds: number[];
    };
    SolverPanel: {
      isDetailedPath: boolean;
    };
    VrpSolution: {
      problem: components["schemas"]["VrpProblem"];
      routes: components["schemas"]["Route"][];
      isEmpty: boolean;
      totalDistance: number;
      isFeasible: boolean;
      totalTime: number;
    };
    VrpSolutionRequest: {
      solution: components["schemas"]["VrpSolution"];
      /** @enum {string} */
      status: "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
      /** Format: uuid */
      solverKey?: string;
    };
    VrpSolverObjective: {
      /** Format: double */
      objective: number;
      solver: string;
      /** @enum {string} */
      status: "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
      /** Format: uuid */
      solverKey: string;
      /** Format: date-time */
      createdAt: string;
    };
    VrpSolverRequest: {
      /** Format: uuid */
      requestKey: string;
      /** Format: int64 */
      problemId: number;
      solver: string;
      /** @enum {string} */
      status: "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
    };
  };
  responses: never;
  parameters: never;
  requestBodies: never;
  headers: never;
  pathItems: never;
}
export type $defs = Record<string, never>;
export interface operations {
  update: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["Vehicle"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  update_1: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["VrpProblem"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  update_2: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["LocationRequest"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  detailedPath: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
        isDetailed: boolean;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  insert: {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["Vehicle"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  copy: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["VrpProblem"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  index: {
    parameters: {
      query?: {
        page?: number;
        size?: number;
        q?: string;
      };
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": components["schemas"]["PageVrpProblemSummary"];
        };
      };
    };
  };
  create: {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["VrpProblem"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  insert_1: {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody: {
      content: {
        "application/json": components["schemas"]["LocationRequest"];
      };
    };
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  terminate: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  solve: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
        solverName: string;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  clear: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  index_1: {
    parameters: {
      query?: {
        page?: number;
        size?: number;
        q?: string;
      };
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "*/*": components["schemas"]["PageVehicle"];
        };
      };
    };
  };
  listByDepot: {
    parameters: {
      query?: {
        ids?: number[];
      };
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "*/*": components["schemas"]["Vehicle"][];
        };
      };
    };
  };
  show: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": components["schemas"]["VrpProblem"];
        };
      };
    };
  };
  index_2: {
    parameters: {
      query?: {
        page?: number;
        size?: number;
        q?: string;
      };
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "*/*": components["schemas"]["PageLocation"];
        };
      };
    };
  };
  list: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        kind: string;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "*/*": components["schemas"]["Location"][];
        };
      };
    };
  };
  solutionState: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": components["schemas"]["PanelSolutionState"];
        };
      };
    };
  };
  solverNames: {
    parameters: {
      query?: never;
      header?: never;
      path?: never;
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": string[];
        };
      };
    };
  };
  solutions: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        problemId: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": components["schemas"]["VrpSolverObjective"][];
        };
      };
    };
  };
  requests: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        problemId: number;
        solverName: string;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content: {
          "application/json": components["schemas"]["VrpSolverRequest"][];
        };
      };
    };
  };
  remove: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  remove_1: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
  remove_2: {
    parameters: {
      query?: never;
      header?: never;
      path: {
        id: number;
      };
      cookie?: never;
    };
    requestBody?: never;
    responses: {
      /** @description OK */
      200: {
        headers: {
          [name: string]: unknown;
        };
        content?: never;
      };
    };
  };
}

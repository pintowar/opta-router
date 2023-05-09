/**
 * This file was auto-generated by openapi-typescript.
 * Do not make direct changes to the file.
 */

export interface paths {
  "/api/solver/{id}/detailed-path/{isDetailed}": {
    put: operations["detailedPath"];
  };
  "/api/solver/{id}/solve": {
    post: operations["solve"];
  };
  "/api/vrp-problems": {
    get: operations["index"];
  };
  "/api/vrp-problems/{id}": {
    get: operations["show"];
  };
  "/api/solver/{id}/terminate": {
    get: operations["terminateEarly"];
  };
  "/api/solver/{id}/solution-panel": {
    get: operations["solutionState"];
  };
  "/api/solver/{id}/clean": {
    get: operations["clean"];
  };
}

export type webhooks = Record<string, never>;

export interface components {
  schemas: {
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
    Depot: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
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
    Vehicle: {
      /** Format: int64 */
      id: number;
      name: string;
      /** Format: int32 */
      capacity: number;
      depot: components["schemas"]["Depot"];
    };
    VrpProblem: {
      /** Format: int64 */
      id: number;
      name: string;
      vehicles: components["schemas"]["Vehicle"][];
      customers: components["schemas"]["Customer"][];
      depots: components["schemas"]["Depot"][];
      locations: components["schemas"]["Location"][];
      /** Format: int32 */
      nlocations: number;
      /** Format: int32 */
      nvehicles: number;
    };
    LatLng: {
      /** Format: double */
      lat: number;
      /** Format: double */
      lng: number;
    };
    PanelSolutionState: {
      solverPanel: components["schemas"]["SolverPanel"];
      solutionState: components["schemas"]["VrpSolutionRegistry"];
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
      instance: components["schemas"]["VrpProblem"];
      routes: components["schemas"]["Route"][];
      empty: boolean;
      totalDistance: number;
      totalTime: Record<string, never>;
    };
    VrpSolutionRegistry: {
      solution: components["schemas"]["VrpSolution"];
      /** @enum {string} */
      state: "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
      /** Format: uuid */
      solverKey?: string;
    };
  };
  responses: never;
  parameters: never;
  requestBodies: never;
  headers: never;
  pathItems: never;
}

export type external = Record<string, never>;

export interface operations {
  detailedPath: {
    parameters: {
      path: {
        id: number;
        isDetailed: boolean;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  solve: {
    parameters: {
      path: {
        id: number;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  index: {
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": components["schemas"]["VrpProblem"][];
        };
      };
    };
  };
  show: {
    parameters: {
      path: {
        id: number;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": components["schemas"]["VrpProblem"];
        };
      };
    };
  };
  terminateEarly: {
    parameters: {
      path: {
        id: number;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
  solutionState: {
    parameters: {
      path: {
        id: number;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": components["schemas"]["PanelSolutionState"];
        };
      };
    };
  };
  clean: {
    parameters: {
      path: {
        id: number;
      };
    };
    responses: {
      /** @description OK */
      200: {
        content: {
          "application/json": "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";
        };
      };
    };
  };
}

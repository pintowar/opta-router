import type { components } from "./generated/api";
type Customer = components["schemas"]["Customer"];
type Depot = components["schemas"]["Depot"];
type Route = components["schemas"]["Route"];
type Vehicle = components["schemas"]["Vehicle"];
type VrpProblem = components["schemas"]["VrpProblem"];
type VrpProblemSummary = components["schemas"]["VrpProblemSummary"];
type VrpSolution = components["schemas"]["VrpSolution"];
type VrpSolverRequest = components["schemas"]["VrpSolverRequest"];
type VrpSolverObjective = components["schemas"]["VrpSolverObjective"];
type PanelSolutionState = components["schemas"]["PanelSolutionState"];
type SolverState = "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";

type VehicleRoute = {
  vehicle?: Vehicle;
  route: Route;
};

type Page<T> = {
  content: T[];
  totalPages: number;
  totalElements: number;
  numberOfElements: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
  empty: boolean;
};

function isDepot(obj: unknown): obj is Depot {
  return Boolean(obj && typeof obj === "object" && !("demand" in obj));
}

export type {
  Customer,
  Depot,
  Page,
  PanelSolutionState,
  Route,
  SolverState,
  Vehicle,
  VehicleRoute,
  VrpProblem,
  VrpProblemSummary,
  VrpSolution,
  VrpSolverRequest,
  VrpSolverObjective,
};

export { isDepot };

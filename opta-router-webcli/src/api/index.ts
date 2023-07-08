import { components } from "./generated/api";
type Route = components["schemas"]["Route"];
type Vehicle = components["schemas"]["Vehicle"];
type VrpProblem = components["schemas"]["VrpProblem"];
type VrpSolution = components["schemas"]["VrpSolution"];
type VrpSolverRequest = components["schemas"]["VrpSolverRequest"];
type VrpSolverObjective = components["schemas"]["VrpSolverObjective"];
type PanelSolutionState = components["schemas"]["PanelSolutionState"];
type SolverState = "ENQUEUED" | "NOT_SOLVED" | "RUNNING" | "TERMINATED";

export type {
  PanelSolutionState,
  Route,
  SolverState,
  Vehicle,
  VrpProblem,
  VrpSolution,
  VrpSolverRequest,
  VrpSolverObjective,
};

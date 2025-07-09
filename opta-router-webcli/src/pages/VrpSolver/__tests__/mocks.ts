import type { PanelSolutionState, VrpProblem, VrpSolution } from "../../../api";

export const mockProblem: VrpProblem = {
  id: 1,
  name: "Test Problem",
  customers: [],
  vehicles: [],
};

export const mockSolution: VrpSolution = {
  problem: mockProblem,
  routes: [],
  totalDistance: 0,
  totalTime: 0,
  isFeasible: true,
  isEmpty: false,
};

export const mockPanelSolutionState: PanelSolutionState = {
  solutionState: {
    status: "NOT_SOLVED",
    solution: mockSolution,
  },
  solverPanel: {
    isDetailedPath: false,
  },
};

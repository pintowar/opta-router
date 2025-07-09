import { fireEvent, render, screen, within } from "@testing-library/vue";
import { useFetch } from "@vueuse/core";
import { beforeEach, describe, expect, it, vi, type Mock } from "vitest";
import { ref, type Ref } from "vue";
import { useRoute } from "vue-router";

import type { Page, VrpProblemSummary } from "../../../api";
import VrpProblems from "../VrpProblems.vue";
import { PaginatedTableMock, VrpCrudPageLayoutMock } from "./mocks";

// Mocks
vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(),
}));

const mockPush = vi.fn();
vi.mock("vue-router", () => ({
  useRoute: vi.fn(),
  useRouter: () => ({
    push: mockPush,
  }),
}));

const mockUseFetch = vi.mocked(useFetch);
const mockUseRoute = vi.mocked(useRoute);

describe("VrpProblems.vue", () => {
  let isFetching: Ref<boolean>, error: Ref<Error | null>, data: Ref<Page<VrpProblemSummary> | null>, execute: Mock;

  beforeEach(() => {
    vi.clearAllMocks();

    isFetching = ref(false);
    error = ref(null);
    data = ref(null);
    execute = vi.fn();

    mockUseRoute.mockReturnValue({
      query: { page: "0", size: "10", q: "" },
      matched: [],
      name: undefined,
      params: {},
      fullPath: "",
      hash: "",
      redirectedFrom: undefined,
      meta: {},
      path: "",
    });

    const getMock = {
      get: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnValue({ isFetching, error, data, execute }),
    };
    mockUseFetch.mockReturnValue(getMock as never);
  });

  const renderComponent = () => {
    return render(VrpProblems, {
      global: {
        stubs: {
          VrpCrudPageLayout: VrpCrudPageLayoutMock,
          PaginatedTable: PaginatedTableMock,
          "v-icon": {
            template: '<i :data-icon-name="name"></i>',
            props: ["name"],
          },
          "router-link": {
            template: '<a :href="to" class="btn btn-sm btn-circle"><slot/></a>',
            props: ["to"],
          },
        },
      },
    });
  };

  it("should display loading state", () => {
    isFetching.value = true;
    renderComponent();
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("should display error message", () => {
    error.value = { message: "Failed to fetch problems" } as Error;
    renderComponent();
    expect(screen.getByRole("alert")).toHaveTextContent("Failed to fetch problems");
  });

  it("should render problems in the table", async () => {
    const problems = [
      {
        id: 1,
        name: "Problem 1",
        nlocations: 5,
        nvehicles: 2,
        totalDemand: 100,
        totalCapacity: 200,
        numSolverRequests: 0,
        numEnqueuedRequests: 0,
        numRunningRequests: 0,
      } as VrpProblemSummary,
    ];
    data.value = {
      content: problems,
      totalElements: 1,
      totalPages: 1,
      numberOfElements: 10,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    };
    renderComponent();
    expect(await screen.findByText("Problem 1")).toBeInTheDocument();
    expect(screen.getByText("5")).toBeInTheDocument(); // nlocations
    expect(screen.getByText("2")).toBeInTheDocument(); // nvehicles
  });

  it('should navigate to new problem page when "New Problem" is clicked', async () => {
    renderComponent();
    await fireEvent.click(screen.getByText("New Problem"));
    expect(mockPush).toHaveBeenCalledWith("/problem/new");
  });

  it("should show correct actions for a problem with no solver requests", async () => {
    const problem: VrpProblemSummary = {
      id: 1,
      name: "Problem 1",
      nLocations: 5,
      nVehicles: 2,
      totalDemand: 100,
      totalCapacity: 200,
      numSolverRequests: 0,
      numEnqueuedRequests: 0,
      numRunningRequests: 0,
      numTerminatedRequests: 0,
      numNotSolvedRequests: 0,
      nlocations: 0,
      nvehicles: 0,
    };
    data.value = {
      content: [problem],
      totalElements: 1,
      totalPages: 1,
      numberOfElements: 10,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    };
    const { container } = renderComponent();

    await screen.findByText("Problem 1");
    const row = container.querySelector("tbody tr");

    const solveLink = row?.querySelector('[data-icon-name="oi-gear"]')?.closest("a");
    expect(solveLink).toHaveAttribute("href", "/solve/1");

    const editLink = row?.querySelector('[data-icon-name="md-edit-twotone"]')?.closest("a");
    expect(editLink).toBeInTheDocument();
    expect(editLink).toHaveAttribute("href", "/problem/1/edit");

    expect(row?.querySelector('[data-icon-name="md-contentcopy"]')).not.toBeInTheDocument();

    const deleteButton = row?.querySelector('[data-icon-name="md-deleteoutline"]')?.closest("button");
    expect(deleteButton).toBeInTheDocument();
    expect(deleteButton).not.toBeDisabled();
  });

  it("should show correct actions for a problem with solver requests", async () => {
    const problem: VrpProblemSummary = {
      id: 2,
      name: "Problem 2",
      nLocations: 5,
      nVehicles: 2,
      totalDemand: 100,
      totalCapacity: 200,
      numSolverRequests: 1,
      numEnqueuedRequests: 0,
      numRunningRequests: 0,
      numTerminatedRequests: 0,
      numNotSolvedRequests: 0,
      nlocations: 0,
      nvehicles: 0,
    };
    data.value = {
      content: [problem],
      totalElements: 1,
      totalPages: 1,
      numberOfElements: 10,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    };
    const { container } = renderComponent();

    await screen.findByText("Problem 2");
    const row = container.querySelector("tbody tr");

    const solveLink = row?.querySelector('[data-icon-name="oi-gear"]')?.closest("a");
    expect(solveLink).toHaveAttribute("href", "/solve/2");

    expect(row?.querySelector('[data-icon-name="md-edit-twotone"]')).not.toBeInTheDocument();

    const copyLink = row?.querySelector('[data-icon-name="md-contentcopy"]')?.closest("a");
    expect(copyLink).toBeInTheDocument();
    expect(copyLink).toHaveAttribute("href", "/problem/2/copy");

    const deleteButton = row?.querySelector('[data-icon-name="md-deleteoutline"]')?.closest("button");
    expect(deleteButton).toBeInTheDocument();
    expect(deleteButton).toBeDisabled();
  });

  it("should display correct status badges", async () => {
    const problems: VrpProblemSummary[] = [
      {
        id: 1,
        name: "P1",
        nlocations: 1,
        nvehicles: 1,
        totalDemand: 1,
        totalCapacity: 1,
        numSolverRequests: 1,
        numEnqueuedRequests: 1,
        numRunningRequests: 0,
        numTerminatedRequests: 0,
        numNotSolvedRequests: 0,
      }, // Enqueued
      {
        id: 2,
        name: "P2",
        nlocations: 1,
        nvehicles: 1,
        totalDemand: 1,
        totalCapacity: 1,
        numSolverRequests: 1,
        numEnqueuedRequests: 0,
        numRunningRequests: 1,
        numTerminatedRequests: 0,
        numNotSolvedRequests: 0,
      }, // Running
      {
        id: 3,
        name: "P3",
        nlocations: 1,
        nvehicles: 1,
        totalDemand: 1,
        totalCapacity: 1,
        numSolverRequests: 0,
        numEnqueuedRequests: 0,
        numRunningRequests: 0,
        numTerminatedRequests: 0,
        numNotSolvedRequests: 0,
      }, // Not Solving
    ];
    data.value = {
      content: problems,
      totalElements: 3,
      totalPages: 3,
      numberOfElements: 10,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    };
    renderComponent();

    await screen.findByText("P1");
    const rows = screen.getAllByRole("row");

    const row1 = within(rows[1]);
    expect(row1.getByText("E")).toBeInTheDocument();
    expect(row1.getByText("E").getAttribute("data-tip")).toBe("Enqueued");

    const row2 = within(rows[2]);
    expect(row2.getByText("R")).toBeInTheDocument();
    expect(row2.getByText("R").getAttribute("data-tip")).toBe("Running");

    const row3 = within(rows[3]);
    expect(row3.getByText("N")).toBeInTheDocument();
    expect(row3.getByText("N").getAttribute("data-tip")).toBe("Not Solving");
  });

  it("should open and close delete dialog", async () => {
    const problem: VrpProblemSummary = {
      id: 1,
      name: "Problem to delete",
      nlocations: 1,
      nvehicles: 1,
      totalDemand: 1,
      totalCapacity: 1,
      numSolverRequests: 0,
      numEnqueuedRequests: 0,
      numRunningRequests: 0,
      numTerminatedRequests: 0,
      numNotSolvedRequests: 0,
    };
    data.value = {
      content: [problem],
      totalElements: 1,
      totalPages: 1,
      numberOfElements: 10,
      size: 10,
      number: 0,
      first: true,
      last: false,
      empty: false,
    };
    const { container } = renderComponent();

    await screen.findByText("Problem to delete");

    expect(screen.queryByText("Delete dialog for Problem to delete")).not.toBeInTheDocument();

    const deleteButton = container.querySelector('[data-icon-name="md-deleteoutline"]')?.closest("button");
    await fireEvent.click(deleteButton!);

    expect(await screen.findByText("Delete dialog for Problem to delete")).toBeInTheDocument();

    await fireEvent.click(screen.getByText("Cancel"));
    expect(screen.queryByText("Delete dialog for Problem to delete")).not.toBeInTheDocument();
  });
});

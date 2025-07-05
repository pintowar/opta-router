import { fireEvent, render, screen, within } from "@testing-library/vue";
import { useFetch } from "@vueuse/core";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { defineComponent, ref } from "vue";
import { useRoute } from "vue-router";

import type { VrpProblemSummary } from "../../../api";
import VrpProblems from "../VrpProblems.vue";

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

const VrpCrudPageLayoutMock = defineComponent({
  name: "VrpCrudPageLayout",
  props: ["isFetching", "error", "removeUrl", "openRemove", "selected"],
  emits: ["toogle-insert", "update:open-remove", "fetch"],
  template: `
    <div>
      <div v-if="isFetching">Loading...</div>
      <div v-if="error" role="alert">{{ error.message }}</div>
      <button @click="$emit('toogle-insert')">New Problem</button>
      <slot />
      <div v-if="openRemove">
        <slot name="delete-dialog">
            <div>Delete dialog for {{ selected?.name }}</div>
            <button @click="$emit('update:open-remove', false)">Cancel</button>
        </slot>
      </div>
    </div>
  `,
});

const PaginatedTableMock = defineComponent({
  name: "PaginatedTable",
  props: ["page"],
  template: `
        <table>
            <thead>
                <tr><slot name="head" /></tr>
            </thead>
            <tbody>
                <tr v-for="item in page?.content" :key="item.id">
                    <slot name="show" :row="item" />
                </tr>
            </tbody>
        </table>
    `,
});

describe("VrpProblems.vue", () => {
  let isFetching: any, error: any, data: any, execute: any;

  beforeEach(() => {
    vi.clearAllMocks();

    isFetching = ref(false);
    error = ref(null);
    data = ref(null);
    execute = vi.fn();

    mockUseRoute.mockReturnValue({
      query: { page: "0", size: "10", q: "" },
    });

    const getMock = {
      get: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnValue({ isFetching, error, data, execute }),
    };
    mockUseFetch.mockReturnValue(getMock as any);
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
    error.value = { message: "Failed to fetch problems" };
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
      },
    ];
    data.value = { content: problems, totalElements: 1 };
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
      nlocations: 5,
      nvehicles: 2,
      totalDemand: 100,
      totalCapacity: 200,
      numSolverRequests: 0,
      numEnqueuedRequests: 0,
      numRunningRequests: 0,
    };
    data.value = { content: [problem] };
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
      nlocations: 5,
      nvehicles: 2,
      totalDemand: 100,
      totalCapacity: 200,
      numSolverRequests: 1,
      numEnqueuedRequests: 0,
      numRunningRequests: 0,
    };
    data.value = { content: [problem] };
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
      }, // Not Solving
    ];
    data.value = { content: problems };
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
    };
    data.value = { content: [problem] };
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

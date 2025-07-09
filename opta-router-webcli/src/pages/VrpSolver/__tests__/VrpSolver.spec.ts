import { config, flushPromises, mount, VueWrapper } from "@vue/test-utils";
import type { AfterFetchContext } from "@vueuse/core";
import { afterEach, beforeEach, describe, expect, it, vi } from "vitest";
import { ref, type MaybeRef } from "vue";

import type { PanelSolutionState } from "../../../api";
import VrpSolver from "../VrpSolver.vue";
import { mockPanelSolutionState, mockSolution } from "./mocks";

// Mocks for useFetch
const mockSolutionPanelData = ref<PanelSolutionState | null>(null);
const mockSolversData = ref<string[]>([]);
const mockSolveStatus = ref<string | null>(null);

const mockDetailedPathExecute = vi.fn().mockResolvedValue(undefined);
const mockSolveExecute = vi.fn().mockResolvedValue(undefined);
const mockTerminateExecute = vi.fn().mockResolvedValue(undefined);
const mockCleanExecute = vi.fn().mockResolvedValue(undefined);
const mockFetchSolversExecute = vi.fn(); // Will be implemented in beforeEach

// Mocks for useWebSocket
const mockWsData = ref<string | null>(null);
const mockWsStatus = ref("CLOSED");
const mockWsOpen = vi.fn();
const mockBeforeFetchCtx = { url: "", options: {}, cancel: vi.fn() };

let afterFetchCallback: ((ctx: AfterFetchContext) => Partial<AfterFetchContext>) | null = null;

vi.mock("@vueuse/core", async (importOriginal) => {
  const actual = await importOriginal<typeof import("@vueuse/core")>();
  return {
    ...actual,
    useFetch: vi.fn((url: MaybeRef<string>, options) => {
      if (options?.afterFetch) {
        afterFetchCallback = options.afterFetch;
      }
      const urlString = typeof url === "string" ? url : url.value;

      const chainable = {
        isFetching: ref(false),
        error: ref(null),
        data: ref(null),
        execute: vi.fn(),
        get: vi.fn().mockReturnThis(),
        put: vi.fn().mockReturnThis(),
        post: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnThis(),
      };

      if (urlString.includes("solution-panel")) {
        chainable.data = mockSolutionPanelData as never;
      } else if (urlString.includes("solver-names")) {
        chainable.data = mockSolversData as never;
        chainable.execute = mockFetchSolversExecute;
      } else if (urlString.includes("detailed-path")) {
        chainable.execute = mockDetailedPathExecute;
      } else if (urlString.includes("terminate")) {
        chainable.execute = mockTerminateExecute;
      } else if (urlString.includes("clean")) {
        chainable.execute = mockCleanExecute;
      } else if (urlString.includes("solve")) {
        chainable.data = mockSolveStatus as never;
        chainable.execute = mockSolveExecute;
      }

      return chainable;
    }),
    useWebSocket: vi.fn(() => ({
      status: mockWsStatus,
      data: mockWsData,
      open: mockWsOpen,
    })),
  };
});

vi.mock("vue-router", () => ({
  useRoute: vi.fn(() => ({ params: { id: "problem-1" } })),
}));

describe("pages/VrpSolver/VrpSolver.vue", () => {
  let wrapper: VueWrapper;

  beforeEach(() => {
    vi.clearAllMocks();
    afterFetchCallback = null;

    mockSolutionPanelData.value = null;
    mockSolversData.value = [];
    mockSolveStatus.value = null;
    mockWsData.value = null;
    mockWsStatus.value = "CLOSED";

    mockFetchSolversExecute.mockImplementation(async () => {
      mockSolversData.value = ["solver-a", "solver-b"];
    });

    config.global.stubs = {
      VrpPageLayout: { template: '<div data-testid="vrp-page-layout"><slot /></div>' },
      VrpSolverPanelLayout: {
        template: '<div data-testid="vrp-solver-panel-layout"><slot name="menu" /><slot name="main" /></div>',
      },
      SolverPanel: {
        name: "SolverPanel",
        props: ["isDetailedPath", "selectedSolver", "solution", "solvers", "solverStatus", "wsStatus"],
        emits: ["update:isDetailedPath", "update:selectedSolver", "onSolve", "onTerminate", "onClear"],
        template: '<div data-testid="solver-panel-stub"></div>',
      },
      SolverMap: {
        name: "SolverMap",
        props: ["solution"],
        template: '<div data-testid="solver-map-stub"></div>',
      },
    };
  });

  afterEach(() => {
    if (wrapper) {
      wrapper.unmount();
    }
    config.global.stubs = {};
  });

  const mountAndInitialize = async () => {
    wrapper = mount(VrpSolver);
    await flushPromises();

    mockSolutionPanelData.value = JSON.parse(JSON.stringify(mockPanelSolutionState));
    await flushPromises();

    if (afterFetchCallback) {
      afterFetchCallback({
        data: mockSolutionPanelData.value,
        response: new Response(),
        context: mockBeforeFetchCtx,
        execute: vi.fn(),
      });
    }
    await flushPromises();
  };

  it("renders child components and initializes data on mount", async () => {
    await mountAndInitialize();

    expect(wrapper.findComponent({ name: "SolverPanel" }).exists()).toBe(true);
    expect(wrapper.findComponent({ name: "SolverMap" }).exists()).toBe(true);

    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    expect(solverPanel.props("solverStatus")).toBe("NOT_SOLVED");
    expect(solverPanel.props("isDetailedPath")).toBe(false);
    expect(solverPanel.props("solution")).toEqual(mockSolution);
    expect(solverPanel.props("solvers")).toEqual(["solver-a", "solver-b"]);
    expect(solverPanel.props("selectedSolver")).toBe("solver-a");

    const solverMap = wrapper.findComponent({ name: "SolverMap" });
    expect(solverMap.props("solution")).toEqual(mockSolution);

    expect(mockFetchSolversExecute).toHaveBeenCalled();
    expect(mockWsOpen).toHaveBeenCalled();
  });

  it("updates solution and status when a websocket message is received", async () => {
    await mountAndInitialize();

    const newSolution = { ...mockSolution, score: "100" };
    const wsPayload = {
      status: "SOLVING",
      solution: newSolution,
    };
    mockWsData.value = JSON.stringify(wsPayload);
    await flushPromises();

    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    expect(solverPanel.props("solverStatus")).toBe("SOLVING");
    expect(solverPanel.props("solution")).toEqual(newSolution);

    const solverMap = wrapper.findComponent({ name: "SolverMap" });
    expect(solverMap.props("solution")).toEqual(newSolution);
  });

  it("calls solve action when SolverPanel emits onSolve", async () => {
    await mountAndInitialize();
    mockSolveStatus.value = "SOLVING_STARTED";

    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    await solverPanel.vm.$emit("onSolve");

    expect(mockSolveExecute).toHaveBeenCalled();
    await flushPromises();
    expect(solverPanel.props("solverStatus")).toBe("SOLVING_STARTED");
  });

  it("calls terminate action when SolverPanel emits onTerminate", async () => {
    await mountAndInitialize();
    // Set status to SOLVING, otherwise terminate is disabled
    mockWsData.value = JSON.stringify({ status: "SOLVING" });
    await flushPromises();
    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    await solverPanel.vm.$emit("onTerminate");
    await flushPromises();
    expect(mockTerminateExecute).toHaveBeenCalled();
  });

  it("calls clean action when SolverPanel emits onClear", async () => {
    await mountAndInitialize();
    // Set status to a state where clean should be possible
    mockWsData.value = JSON.stringify({ status: "TERMINATED" });
    await flushPromises();
    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    await solverPanel.vm.$emit("onClear");
    await flushPromises();
    expect(mockCleanExecute).toHaveBeenCalled();
  });

  it("calls detailed path endpoint when isDetailedPath changes", async () => {
    await mountAndInitialize();
    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });
    await solverPanel.vm.$emit("update:isDetailedPath", true);
    await flushPromises();
    expect(mockDetailedPathExecute).toHaveBeenCalled();
  });

  it("does not call actions if problem is not loaded", async () => {
    // Don't call mountAndInitialize here
    wrapper = mount(VrpSolver);
    await flushPromises();

    const solverPanel = wrapper.findComponent({ name: "SolverPanel" });

    await solverPanel.vm.$emit("onSolve");
    expect(mockSolveExecute).not.toHaveBeenCalled();

    await solverPanel.vm.$emit("onTerminate");
    expect(mockTerminateExecute).not.toHaveBeenCalled();

    await solverPanel.vm.$emit("onClear");
    expect(mockCleanExecute).not.toHaveBeenCalled();

    await solverPanel.vm.$emit("update:isDetailedPath", true);
    expect(mockDetailedPathExecute).not.toHaveBeenCalled();
  });
});

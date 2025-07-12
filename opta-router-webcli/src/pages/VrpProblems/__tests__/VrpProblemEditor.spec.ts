import { render, screen, waitFor } from "@testing-library/vue";
import { type AfterFetchContext, type UseFetchOptions, useFetch } from "@vueuse/core";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { type Ref, ref } from "vue";
import { useRoute } from "vue-router";

import type { VrpProblem } from "../../../api";
import VrpProblemEditor from "../VrpProblemEditor.vue";
import { VrpPageLayoutMock, VrpProblemFormMock } from "./mocks";

// Mocks
vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(),
}));

vi.mock("vue-router", () => ({
  useRoute: vi.fn(),
}));

const mockUseFetch = vi.mocked(useFetch);
const mockUseRoute = vi.mocked(useRoute);
const mockBeforeFetchCtx = { url: "", options: {}, cancel: vi.fn() };

describe("VrpProblemEditor.vue", () => {
  let isFetching: Ref<boolean>,
    error: Ref<Error | null>,
    data: Ref<VrpProblem | null>,
    getMock: { get: () => unknown; json: () => unknown };

  beforeEach(() => {
    vi.clearAllMocks();

    isFetching = ref(false);
    error = ref(null);
    data = ref(null);

    getMock = {
      get: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnValue({ isFetching, error, data }),
    };

    mockUseFetch.mockReturnValue(getMock as never);
    mockUseRoute.mockReturnValue({
      query: { page: "0", size: "10", q: "" },
      matched: [],
      name: undefined,
      params: { id: "1" },
      fullPath: "",
      hash: "",
      redirectedFrom: undefined,
      meta: {},
      path: "",
    });
  });

  const renderComponent = (mode: "create" | "update" | "copy") => {
    return render(VrpProblemEditor, {
      props: { mode },
      global: {
        stubs: {
          VrpPageLayout: VrpPageLayoutMock,
          VrpProblemForm: VrpProblemFormMock,
        },
      },
    });
  };

  it("should display loading state in update mode", () => {
    isFetching.value = true;
    renderComponent("update");
    expect(screen.getByText("Loading...")).toBeInTheDocument();
  });

  it("should display error message in copy mode", () => {
    error.value = { message: "Failed to fetch problem" } as Error;
    renderComponent("copy");
    expect(screen.getByRole("alert")).toHaveTextContent("Failed to fetch problem");
  });

  describe("create mode", () => {
    it("should not fetch data immediately", () => {
      renderComponent("create");
      expect(useFetch).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ immediate: false }));
    });

    it("should render the form with a default problem", async () => {
      renderComponent("create");
      expect(await screen.findByText("Persist URL: /api/vrp-problems")).toBeInTheDocument();
      expect(screen.getByRole("heading")).toHaveTextContent(""); // Default name is empty
    });
  });

  describe("update mode", () => {
    it("should fetch data immediately", () => {
      renderComponent("update");
      expect(useFetch).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ immediate: true }));
    });

    it("should render the form with fetched data", async () => {
      const problem: VrpProblem = { id: 1, name: "Problem 1", vehicles: [], customers: [] };
      data.value = problem;
      renderComponent("update");

      expect(await screen.findByText("Problem 1")).toBeInTheDocument();
      expect(screen.getByText("Persist URL: /api/vrp-problems/1/update")).toBeInTheDocument();
    });
  });

  describe("copy mode", () => {
    it("should fetch data immediately", () => {
      renderComponent("copy");
      expect(useFetch).toHaveBeenCalledWith(expect.anything(), expect.objectContaining({ immediate: true }));
    });

    it("should render the form with fetched data", async () => {
      const problem: VrpProblem = { id: 1, name: "Problem 1 Copy", vehicles: [], customers: [] };
      data.value = problem;
      renderComponent("copy");

      expect(await screen.findByText("Problem 1 Copy")).toBeInTheDocument();
      expect(screen.getByText("Persist URL: /api/vrp-problems/1/copy")).toBeInTheDocument();
    });
  });

  it("should handle afterFetch correctly when data is partial", async () => {
    const partialProblem = { id: 1, name: "Partial Problem", vehicles: [], customers: [] };
    const useFetchOptions: UseFetchOptions = {};
    mockUseFetch.mockImplementation((_url, options) => {
      Object.assign(useFetchOptions, options);
      return getMock as never;
    });

    renderComponent("update");
    const ctx = {
      data: partialProblem,
      response: new Response(),
      context: mockBeforeFetchCtx,
      execute: vi.fn(),
    };
    const result = useFetchOptions.afterFetch?.(ctx) as AfterFetchContext<VrpProblem>;

    await waitFor(() => {
      expect(result?.data).toEqual({
        id: 1,
        name: "Partial Problem",
        vehicles: [],
        customers: [],
      });
    });
  });

  it("should handle afterFetch correctly when data is null", async () => {
    const useFetchOptions: UseFetchOptions = {};
    mockUseFetch.mockImplementation((_url, options) => {
      Object.assign(useFetchOptions, options);
      return getMock as never;
    });

    renderComponent("update");
    const ctx = {
      data: null,
      response: new Response(),
      context: mockBeforeFetchCtx,
      execute: vi.fn(),
    };
    const result = useFetchOptions.afterFetch?.(ctx) as AfterFetchContext<VrpProblem>;

    await waitFor(() => {
      expect(result?.data).toEqual({
        id: -1,
        name: "",
        vehicles: [],
        customers: [],
      });
    });
  });
});

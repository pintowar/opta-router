import { render, screen, waitFor } from "@testing-library/vue";
import { useFetch } from "@vueuse/core";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { defineComponent, ref } from "vue";
import { useRoute } from "vue-router";

import type { VrpProblem } from "../../../api";
import VrpProblemEditor from "../VrpProblemEditor.vue";

// Mocks
vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(),
}));

vi.mock("vue-router", () => ({
  useRoute: vi.fn(),
}));

const mockUseFetch = vi.mocked(useFetch);
const mockUseRoute = vi.mocked(useRoute);

const VrpPageLayoutMock = defineComponent({
  name: "VrpPageLayout",
  props: ["isFetching", "error"],
  template: `
    <div>
      <div v-if="isFetching">Loading...</div>
      <div v-if="error" role="alert">{{ error.message }}</div>
      <slot />
    </div>
  `,
});

const VrpProblemFormMock = defineComponent({
  name: "VrpProblemForm",
  props: ["problem", "persistUrl"],
  template: `
    <div>
      <h2>{{ problem.name }}</h2>
      <span>Persist URL: {{ persistUrl }}</span>
    </div>
  `,
});

describe("VrpProblemEditor.vue", () => {
  let isFetching: any, error: any, data: any, getMock: any;

  beforeEach(() => {
    vi.clearAllMocks();

    isFetching = ref(false);
    error = ref(null);
    data = ref(null);

    getMock = {
      get: vi.fn().mockReturnThis(),
      json: vi.fn().mockReturnValue({ isFetching, error, data }),
    };

    mockUseFetch.mockReturnValue(getMock);
    mockUseRoute.mockReturnValue({
      query: { page: "0", size: "10", q: "" },
      matched: [],
      name: undefined,
      params: { id: "1" },
      fullPath: "",
      hash: "",
      redirectedFrom: undefined,
      meta: {},
      path: ""
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
    error.value = { message: "Failed to fetch problem" };
    renderComponent("copy");
    expect(screen.getByRole("alert")).toHaveTextContent("Failed to fetch problem");
  });

  describe("create mode", () => {
    it("should not fetch data immediately", () => {
      renderComponent("create");
      expect(useFetch).toHaveBeenCalledWith(expect.any(Object), expect.objectContaining({ immediate: false }));
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
      expect(useFetch).toHaveBeenCalledWith(expect.any(Object), expect.objectContaining({ immediate: true }));
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
      expect(useFetch).toHaveBeenCalledWith(expect.any(Object), expect.objectContaining({ immediate: true }));
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
    const partialProblem = { name: "Partial Problem" };
    const useFetchOptions: any = {};
    mockUseFetch.mockImplementation((_url, options) => {
      Object.assign(useFetchOptions, options);
      return getMock;
    });

    renderComponent("update");
    const ctx = { data: partialProblem, response: new Response() };
    const result = useFetchOptions.afterFetch(ctx);

    await waitFor(() => {
      expect(result.data).toEqual({
        id: -1,
        name: "Partial Problem",
        vehicles: [],
        customers: [],
      });
    });
  });

  it("should handle afterFetch correctly when data is null", async () => {
    const useFetchOptions: any = {};
    mockUseFetch.mockImplementation((_url, options) => {
      Object.assign(useFetchOptions, options);
      return getMock;
    });

    renderComponent("update");
    const ctx = { data: null, response: new Response() };
    const result = useFetchOptions.afterFetch(ctx);

    await waitFor(() => {
      expect(result.data).toEqual({
        id: -1,
        name: "",
        vehicles: [],
        customers: [],
      });
    });
  });
});

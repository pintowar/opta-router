import type { Ref } from "vue";
import { ref } from "vue";

import { flushPromises, mount, RouterLinkStub } from "@vue/test-utils";
import { useFetch } from "@vueuse/core";
import type { Mock } from "vitest";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { useRoute } from "vue-router";
import VrpSolverHistory from "../VrpSolverHistory.vue";

vi.mock("@vueuse/core", () => ({
  useFetch: vi.fn(),
  useColorMode: vi.fn().mockReturnValue(ref("light")),
}));
vi.mock("vue-router", () => ({
  useRoute: vi.fn(),
}));
vi.mock("../../layout", () => ({
  VrpPageLayout: {
    template: "<div><slot /></div>",
    props: ["isFetching", "error"],
  },
  VrpSolverPanelLayout: {
    template: "<div><slot name='menu' /><slot name='main' /></div>",
  },
}));
vi.mock("./SolutionsHistoryChart.vue", () => ({
  default: {
    template: "<div></div>",
    props: ["solutions", "request", "solvers"],
  },
}));

describe("VrpSolverHistory", () => {
  beforeEach(() => {
    (useFetch as Mock).mockImplementation(() => {
      const mockReturnValue = {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({
          data: ref([]),
          isFetching: ref(false),
          error: ref(null),
        }),
      };

      return mockReturnValue;
    });
    vi.mocked(useRoute).mockReturnValue({
      params: { id: "123" },
    } as never);
  });

  it("renders correctly", () => {
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    expect(wrapper.exists()).toBe(true);
  });

  it("fetches and displays solvers", async () => {
    const solutions = ref([
      { solver: "solver1", solverKey: "key1" },
      { solver: "solver2", solverKey: "key2" },
    ]);
    (useFetch as Mock).mockImplementation((url: Ref<string>) => {
      if (url.value.includes("solutions")) {
        return {
          get: vi.fn().mockReturnThis(),
          json: vi.fn().mockReturnValue({
            data: solutions,
            isFetching: ref(false),
            error: ref(null),
          }),
        } as never;
      }
      return {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({
          data: ref([]),
          isFetching: ref(false),
          error: ref(null),
        }),
      } as never;
    });
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    await flushPromises();
    const select = wrapper.findAll("select")[0];
    expect(select).toBeTruthy();
    const options = select!.findAll("option");
    expect(options.length).toBe(3); // all + 2 solvers
    const option1 = options[1];
    expect(option1).toBeTruthy();
    expect(option1!.text()).toBe("solver1");
    const option2 = options[2];
    expect(option2).toBeTruthy();
    expect(option2!.text()).toBe("solver2");
  });

  it("fetches and displays requests", async () => {
    const requests = ref([
      { requestKey: "req1", status: "ENQUEUED" },
      { requestKey: "req2", status: "RUNNING" },
    ]);
    (useFetch as Mock).mockImplementation((url: Ref<string>) => {
      if (url.value.includes("requests")) {
        return {
          get: vi.fn().mockReturnThis(),
          json: vi.fn().mockReturnValue({
            data: requests,
            isFetching: ref(false),
            error: ref(null),
          }),
        } as never;
      }
      return {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({
          data: ref([]),
          isFetching: ref(false),
          error: ref(null),
        }),
      } as never;
    });
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    await flushPromises();
    const select = wrapper.findAll("select")[1];
    expect(select).toBeTruthy();
    const options = select!.findAll("option");
    expect(options.length).toBe(2);
    const option1 = options[0];
    expect(option1).toBeTruthy();
    expect(option1!.text()).toBe("req1");
    const option2 = options[1];
    expect(option2).toBeTruthy();
    expect(option2!.text()).toBe("req2");
  });

  it("filters solutions based on selected solver", async () => {
    const solutions = ref([
      { solver: "solver1", solverKey: "key1" },
      { solver: "solver2", solverKey: "key2" },
    ]);
    (useFetch as Mock).mockImplementation((url: Ref<string>) => {
      if (url.value.includes("solutions")) {
        return {
          get: vi.fn().mockReturnThis(),
          json: vi.fn().mockReturnValue({
            data: solutions,
            isFetching: ref(false),
            error: ref(null),
          }),
        } as never;
      }
      return {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({
          data: ref([]),
          isFetching: ref(false),
          error: ref(null),
        }),
      } as never;
    });
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    await flushPromises();
    const select = wrapper.findAll("select")[0];
    expect(select).toBeTruthy();
    await select!.setValue("solver1");
    await flushPromises();
    const chart = wrapper.findComponent({ name: "SolutionsHistoryChart" });
    expect(chart.props("solutions").length).toBe(1);
    expect(chart.props("solutions")[0].solver).toBe("solver1");
  });

  it("displays correct status for each request", async () => {
    const requests = ref([
      { requestKey: "req1", status: "ENQUEUED" },
      { requestKey: "req2", status: "RUNNING" },
      { requestKey: "req3", status: "TERMINATED" },
      { requestKey: "req4", status: "NOT_SOLVED" },
    ]);
    (useFetch as Mock).mockImplementation((url: Ref<string>) => {
      if (url.value.includes("requests")) {
        return {
          get: vi.fn().mockReturnThis(),
          json: vi.fn().mockReturnValue({
            data: requests,
            isFetching: ref(false),
            error: ref(null),
          }),
        } as never;
      }
      return {
        get: vi.fn().mockReturnThis(),
        json: vi.fn().mockReturnValue({
          data: ref([]),
          isFetching: ref(false),
          error: ref(null),
        }),
      } as never;
    });
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    await flushPromises();
    const select = wrapper.findAll("select")[1];
    expect(select).toBeTruthy();
    const options = select!.findAll("option");
    const option0 = options[0];
    expect(option0).toBeTruthy();
    expect(option0!.classes()).toContain("text-info");
    const option1 = options[1];
    expect(option1).toBeTruthy();
    expect(option1!.classes()).toContain("text-success");
    const option2 = options[2];
    expect(option2).toBeTruthy();
    expect(option2!.classes()).toContain("text-warning");
    const option3 = options[3];
    expect(option3).toBeTruthy();
    expect(option3!.classes()).toContain("text-error");
  });

  it("navigates to solver page when link is clicked", async () => {
    const wrapper = mount(VrpSolverHistory, {
      global: {
        stubs: {
          "router-link": RouterLinkStub,
          apexchart: true,
        },
      },
    });
    const link = wrapper.findComponent(RouterLinkStub);
    expect(link.props("to")).toBe("/solve/123");
  });
});

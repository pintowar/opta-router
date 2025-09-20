import { mount } from "@vue/test-utils";
import { describe, expect, it, vi } from "vitest";
import { createRouter, createWebHistory } from "vue-router";
import PaginatedTable from "../PaginatedTable.vue";

const router = createRouter({
  history: createWebHistory(),
  routes: [{ path: "/", component: { template: "<div></div>" } }],
});

describe("PaginatedTable", () => {
  it("renders correctly with empty page data", async () => {
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 0,
          size: 10,
          numberOfElements: 0,
          empty: true,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        },
      },
      slots: {
        head: "<th>Test Head</th>",
        show: "<td>Test Show</td>",
        edit: "<td>Test Edit</td>",
        foot: "<td>Test Foot</td>",
      },
      global: {
        plugins: [router],
      },
    });

    expect(wrapper.find("table").exists()).toBe(true);
    expect(wrapper.find("thead").text()).toContain("Test Head");
    expect(wrapper.find("tfoot").text()).toContain("Test Foot");
    expect(wrapper.find("tbody").text()).not.toContain("Test Show");
    expect(wrapper.find("tbody").text()).not.toContain("Test Edit");
    expect(wrapper.find(".join-item.btn").exists()).toBe(true);
  });

  it("renders rows when page content is provided", async () => {
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [{ id: 1, name: "Item 1" }],
          number: 0,
          size: 10,
          numberOfElements: 1,
          empty: false,
          totalElements: 1,
          totalPages: 1,
          first: true,
          last: true,
        },
      },
      slots: {
        head: "<th>Test Head</th>",
        show: '<template #show="{ row }"><td>{{ row.name }}</td></template>',
        edit: "<td>Test Edit</td>",
        foot: "<td>Test Foot</td>",
      },
      global: {
        plugins: [router],
      },
    });

    expect(wrapper.find("tbody").text()).toContain("Item 1");
  });

  it("displays edit slot when isEditing is true and row is selected", async () => {
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [{ id: 1, name: "Item 1" }],
          number: 0,
          size: 10,
          numberOfElements: 1,
          empty: false,
          totalElements: 1,
          totalPages: 1,
          first: true,
          last: true,
        },
        selected: { id: 1, name: "Item 1" },
        isEditing: true,
      },
      slots: {
        head: "<th>Test Head</th>",
        show: '<template #show="{ row }"><td>{{ row.name }}</td></template>',
        edit: "<td>Editing Row</td>",
        foot: "<td>Test Foot</td>",
      },
      global: {
        plugins: [router],
      },
    });

    expect(wrapper.find("tbody").text()).toContain("Editing Row");
    expect(wrapper.find("tbody").text()).not.toContain("Item 1");
  });

  it("changes page size when select input is changed", async () => {
    const pushSpy = vi.spyOn(router, "push");
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 0,
          size: 10,
          numberOfElements: 0,
          empty: true,
          totalElements: 0,
          totalPages: 0,
          first: true,
          last: true,
        },
      },
      global: {
        plugins: [router],
      },
    });

    const select = wrapper.find("#page-size");
    await select.setValue("25");

    expect(pushSpy).toHaveBeenCalledWith({
      query: {
        size: 25,
      },
    });
  });

  it("navigates to next page when next button is clicked", async () => {
    const pushSpy = vi.spyOn(router, "push");
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 0,
          size: 10,
          numberOfElements: 20,
          empty: false,
          totalElements: 20,
          totalPages: 2,
          first: true,
          last: false,
        },
      },
      global: {
        plugins: [router],
      },
    });

    const nextButton = wrapper.findAll(".join-item.btn")[3];
    expect(nextButton).toBeTruthy();
    await nextButton!.trigger("click"); // Next button

    expect(pushSpy).toHaveBeenCalledWith({
      query: {
        page: 1,
      },
    });
  });

  it("navigates to previous page when previous button is clicked", async () => {
    const pushSpy = vi.spyOn(router, "push");
    await router.push("/?page=1");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 1,
          size: 10,
          numberOfElements: 20,
          empty: false,
          totalElements: 20,
          totalPages: 2,
          first: false,
          last: true,
        },
      },
      global: {
        plugins: [router],
      },
    });

    const prevButton = wrapper.findAll(".join-item.btn")[1];
    expect(prevButton).toBeTruthy();
    await prevButton!.trigger("click"); // Previous button

    expect(pushSpy).toHaveBeenCalledWith({
      query: {
        page: 0,
      },
    });
  });

  it("navigates to first page when first button is clicked", async () => {
    const pushSpy = vi.spyOn(router, "push");
    await router.push("/?page=2");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 2,
          size: 10,
          numberOfElements: 30,
          empty: false,
          totalElements: 30,
          totalPages: 3,
          first: false,
          last: true,
        },
      },
      global: {
        plugins: [router],
      },
    });

    const firstButton = wrapper.findAll(".join-item.btn")[0];
    expect(firstButton).toBeTruthy();
    await firstButton!.trigger("click"); // First button

    expect(pushSpy).toHaveBeenCalledWith({
      query: {
        page: 0,
      },
    });
  });

  it("navigates to last page when last button is clicked", async () => {
    const pushSpy = vi.spyOn(router, "push");
    await router.push("/");
    const wrapper = mount(PaginatedTable, {
      props: {
        page: {
          content: [],
          number: 0,
          size: 10,
          numberOfElements: 30,
          empty: false,
          totalElements: 30,
          totalPages: 3,
          first: true,
          last: false,
        },
      },
      global: {
        plugins: [router],
      },
    });

    const lastButton = wrapper.findAll(".join-item.btn")[4];
    expect(lastButton).toBeTruthy();
    await lastButton!.trigger("click"); // Last button

    expect(pushSpy).toHaveBeenCalledWith({
      query: {
        page: 2,
      },
    });
  });
});

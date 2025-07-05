import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import VrpLocationForm from "../VrpLocationForm.vue";

describe("VrpLocationForm.vue", () => {
  const mockCustomer = {
    id: 1,
    name: "Customer 1",
    lat: 10,
    lng: 20,
    demand: 100,
  };

  const mockDepot = {
    id: 2,
    name: "Depot 1",
    lat: 30,
    lng: 40,
  };

  it("renders the form with initial customer data", async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: mockCustomer,
        isLoading: false,
      },
    });

    await nextTick();

    expect(wrapper.find('input[name="name"]').element.value).toBe("Customer 1");
    expect(wrapper.find('input[name="lat"]').element.value).toBe("10");
    expect(wrapper.find('input[name="lng"]').element.value).toBe("20");
    expect(wrapper.find('input[name="demand"]').element.value).toBe("100");
  });

  it("renders the form with initial depot data", async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: mockDepot,
        isLoading: false,
      },
    });

    await nextTick();

    expect(wrapper.find('input[name="name"]').element.value).toBe("Depot 1");
    expect(wrapper.find('input[name="lat"]').element.value).toBe("30");
    expect(wrapper.find('input[name="lng"]').element.value).toBe("40");
    expect(wrapper.find('input[name="demand"]').exists()).toBe(false);
  });

  it('emits "update:location" when an input value changes', async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: { ...mockCustomer },
        isLoading: false,
      },
    });

    await wrapper.find('input[name="name"]').setValue("New Name");
    await wrapper.vm.$emit("update:location", { ...mockCustomer, name: "New Name" });

    expect(wrapper.emitted("update:location")).toHaveLength(1);
    const emittedLocation = wrapper.emitted("update:location")[0][0];
    expect(emittedLocation.name).toBe("New Name");
  });

  it('emits "close" when the close button is clicked', async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: mockCustomer,
        isLoading: false,
      },
    });

    await wrapper.find("button.btn-sm:not(.btn-success)").trigger("click");
    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it('emits "execute" when the save button is clicked', async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: mockCustomer,
        isLoading: false,
      },
    });

    await wrapper.find("button.btn-success").trigger("click");
    expect(wrapper.emitted("execute")).toHaveLength(1);
  });

  it("disables form elements when isLoading is true", async () => {
    const wrapper = mount(VrpLocationForm, {
      props: {
        location: mockCustomer,
        isLoading: true,
      },
    });

    expect(wrapper.find('input[name="name"]').element.disabled).toBe(true);
    expect(wrapper.find('input[name="lat"]').element.disabled).toBe(true);
    expect(wrapper.find('input[name="lng"]').element.disabled).toBe(true);
    expect(wrapper.find('input[name="demand"]').element.disabled).toBe(true);
    expect(wrapper.find("button.btn-success").element.disabled).toBe(true);
  });
});

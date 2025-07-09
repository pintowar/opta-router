import { mount } from "@vue/test-utils";
import { describe, expect, it } from "vitest";
import { nextTick } from "vue";
import VrpVehicleForm from "../VrpVehicleForm.vue";

describe("VrpVehicleForm.vue", () => {
  const mockVehicle = {
    id: 1,
    name: "Vehicle 1",
    capacity: 100,
    depot: { id: 1, name: "Depot 1", lat: 0, lng: 0 },
  };

  const mockDepots = [
    { id: 1, name: "Depot 1", lat: 0, lng: 0 },
    { id: 2, name: "Depot 2", lat: 0, lng: 0 },
  ];

  it("renders the form with initial vehicle data", async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: mockVehicle,
        isLoading: false,
        depots: mockDepots,
      },
    });

    await nextTick();

    expect((wrapper.find('input[name="name"]').element as HTMLInputElement).value).toBe("Vehicle 1");
    expect((wrapper.find('input[name="capacity"]').element as HTMLInputElement).value).toBe("100");
    const select = wrapper.find("select").element;
    expect(select.value).toBe("[object Object]"); // Depot object is the value
  });

  it('emits "update:vehicle" when an input value changes', async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: { ...mockVehicle },
        isLoading: false,
        depots: mockDepots,
      },
    });

    await wrapper.find('input[name="name"]').setValue("New Name");
    await wrapper.vm.$emit("update:vehicle", { ...mockVehicle, name: "New Name" });

    expect(wrapper.emitted("update:vehicle")).toHaveLength(1);
    const emittedVehicle: any = wrapper.emitted("update:vehicle")![0][0];
    expect(emittedVehicle.name).toBe("New Name");

    await wrapper.find('input[name="capacity"]').setValue(200);
    await wrapper.vm.$emit("update:vehicle", { ...mockVehicle, capacity: 200 });
    expect(wrapper.emitted("update:vehicle")).toHaveLength(2);
    const emittedVehicle2: any = wrapper.emitted("update:vehicle")![1][0];
    expect(emittedVehicle2.capacity).toBe(200);
  });

  it('emits "close" when the close button is clicked', async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: mockVehicle,
        isLoading: false,
        depots: mockDepots,
      },
    });

    await wrapper.find("button.btn-sm:not(.btn-success)").trigger("click");
    expect(wrapper.emitted("close")).toHaveLength(1);
  });

  it('emits "execute" when the save button is clicked', async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: mockVehicle,
        isLoading: false,
        depots: mockDepots,
      },
    });

    await wrapper.find("button.btn-success").trigger("click");
    expect(wrapper.emitted("execute")).toHaveLength(1);
  });

  it("disables form elements when isLoading is true", async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: mockVehicle,
        isLoading: true,
        depots: mockDepots,
      },
    });

    expect((wrapper.find('input[name="name"]').element as HTMLInputElement).disabled).toBe(true);
    expect((wrapper.find('input[name="capacity"]').element as HTMLInputElement).disabled).toBe(true);
    expect((wrapper.find("button.btn-success").element as HTMLButtonElement).disabled).toBe(true);
  });

  it("does not disable the close button when isLoading is true", async () => {
    const wrapper = mount(VrpVehicleForm, {
      props: {
        vehicle: mockVehicle,
        isLoading: true,
        depots: mockDepots,
      },
    });

    expect((wrapper.find("button.btn-sm:not(.btn-success)").element as HTMLButtonElement).disabled).toBe(false);
  });
});

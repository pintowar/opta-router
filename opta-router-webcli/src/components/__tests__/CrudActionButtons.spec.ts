import { fireEvent, render, screen } from "@testing-library/vue";
import { OhVueIcon } from "oh-vue-icons";
import CrudActionButtons from "../CrudActionButtons.vue";

describe("CrudActionButtons", () => {
  it("renders edit and delete buttons by default", () => {
    render(CrudActionButtons, {
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    expect(screen.getByTestId("edit-button")).toBeInTheDocument();
    expect(screen.getByTestId("delete-button")).toBeInTheDocument();
  });

  it("emits edit and delete events", async () => {
    const { emitted } = render(CrudActionButtons, {
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });

    await fireEvent.click(screen.getByTestId("edit-button"));
    expect(emitted().edit).toBeTruthy();

    await fireEvent.click(screen.getByTestId("delete-button"));
    expect(emitted().delete).toBeTruthy();
  });

  it("renders update and cancel buttons when editing", () => {
    render(CrudActionButtons, {
      props: {
        isEditing: true,
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    expect(screen.getByTestId("update-button")).toBeInTheDocument();
    expect(screen.getByTestId("cancel-button")).toBeInTheDocument();
  });

  it("emits update and cancel events when editing", async () => {
    const { emitted } = render(CrudActionButtons, {
      props: {
        isEditing: true,
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });

    await fireEvent.click(screen.getByTestId("update-button"));
    expect(emitted().update).toBeTruthy();

    await fireEvent.click(screen.getByTestId("cancel-button"));
    expect(emitted().cancel).toBeTruthy();
  });

  it("disables the update button when updating", () => {
    render(CrudActionButtons, {
      props: {
        isEditing: true,
        isUpdating: true,
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });
    expect(screen.getByTestId("update-button")).toBeDisabled();
  });
});

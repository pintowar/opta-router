import { fireEvent, render, screen } from "@testing-library/vue";
import { OhVueIcon } from "oh-vue-icons";
import AlertMessage from "../AlertMessage.vue";

describe("AlertMessage", () => {
  it("renders the message and variant", () => {
    render(AlertMessage, {
      props: {
        message: "Test message",
        variant: "success",
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });

    expect(screen.getByText("Test message")).toBeInTheDocument();
    expect(screen.getByText("success")).toBeInTheDocument();
  });

  it("emits a close event when the close button is clicked", async () => {
    const { emitted } = render(AlertMessage, {
      props: {
        message: "Test message",
        closable: true,
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });

    await fireEvent.click(screen.getByText("X"));
    expect(emitted().close).toBeTruthy();
  });

  it("does not render the close button when closable is false", () => {
    render(AlertMessage, {
      props: {
        message: "Test message",
        closable: false,
      },
      global: {
        components: {
          "v-icon": OhVueIcon,
        },
      },
    });

    expect(screen.queryByText("X")).not.toBeInTheDocument();
  });
});

import java.lang.reflect.Method;
public class FindMethods {
    public static void main(String[] args) throws Exception {
        Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen");
        for (Method m : screenClass.getDeclaredMethods()) {
            if (m.getName().toLowerCase().contains("left") || m.getName().toLowerCase().contains("top")) {
                System.out.println(m.getName());
            }
        }
    }
}

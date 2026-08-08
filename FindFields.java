import java.lang.reflect.Field;
public class FindFields {
    public static void main(String[] args) throws Exception {
        Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.inventory.AbstractContainerScreen");
        for (Field f : screenClass.getDeclaredFields()) {
            if (f.getType() == int.class) {
                System.out.println(f.getName() + " " + f.getType().getName());
            }
        }
    }
}

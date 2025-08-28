//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        UndoableStringBuilder usb = new UndoableStringBuilder("Hello");
        usb.append(' ').append("world");           // "Hello world"
        usb.delete(5, 6);                           // "Helloworld"
        usb.insert(5, ", ");                        // "Hello, world"
        System.out.println(usb);                    // Hello, world

        usb.undo();                                 // откат вставки
        System.out.println(usb);                    // Helloworld

        usb.undo();                                 // откат удаления
        System.out.println(usb);                    // Hello world

        usb.undo();                                 // откат второго append
        System.out.println(usb);                    // Hello

    }
}
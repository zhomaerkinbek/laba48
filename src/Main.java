import laba.Laba48;

import java.io.IOException;

public class Main {
    public static void main(String[] args){
        try {
            new Laba48("localhost", 9889).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

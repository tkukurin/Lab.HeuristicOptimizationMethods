import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static int vehicleNum;
    private static int trackNum;
    private static List<Vehicle> vehicles;
    private static List<Track> tracks;
    private static HashMap<Integer, ArrayList<Integer>> blockades;


    public static void main(String[] args) throws FileNotFoundException {
        initialise();
        Scanner sc = new Scanner(new FileReader("instanca1.txt"));
        vehicleNum = Integer.parseInt(sc.nextLine().trim());
        trackNum = Integer.parseInt(sc.nextLine().trim());
        sc.nextLine();
        String[] vehicleLengths = sc.nextLine().trim().split(" ");
        sc.nextLine();
        String[] series = sc.nextLine().trim().split(" ");
        sc.nextLine();
        List<String> limitations = new ArrayList<>();
        for (int i = 0; i < vehicleNum; i++) {
            limitations.add(sc.nextLine());
        }
        sc.nextLine();
        String[] trackLengths = sc.nextLine().trim().split(" ");
        sc.nextLine();
        String[] departureTimes = sc.nextLine().trim().split(" ");
        sc.nextLine();
        String[] layoutTypes = sc.nextLine().trim().split(" ");
        sc.nextLine();
        while(sc.hasNext()) {
            String[] lines = sc.nextLine().trim().split(" ");
            if (lines.length > 0 && !lines[0].isEmpty()) {
                ArrayList<Integer> blocks = new ArrayList<>();
                for (int i = 1; i < lines.length; i++) {
                    blocks.add(Integer.parseInt(lines[i]));
                }
                blockades.put(Integer.parseInt(lines[0]), blocks);
            }
        }
        for (int i = 0; i < vehicleNum; i++) {
            int len = Integer.parseInt(vehicleLengths[i]);
            int ser = Integer.parseInt(series[i]);
            int dep = Integer.parseInt(departureTimes[i]);
            int lay = Integer.parseInt(layoutTypes[i]);
            vehicles.add(new Vehicle(len, ser, dep, lay));
        }
        for (int i = 0; i < trackNum; i++) {
            ArrayList<Integer> rest = new ArrayList<>();
            for (int j = 0; j < vehicleNum; j++) {
                String[] lim = limitations.get(j).trim().split(" ");
                rest.add(Integer.parseInt(lim[i]));
            }
            int len = Integer.parseInt(trackLengths[i].trim());
            tracks.add(new Track(rest, len));
        }
        sc.close();

        System.out.println("Vehicles:");
        System.out.println("-----------------------------------------");
        for (Vehicle v : vehicles) {
            System.out.println(v.toString());
        }
        System.out.println("Tracks:");
        System.out.println("-----------------------------------------");
        for (Track t : tracks) {
            System.out.println(t.toString());
        }
    }

    private static void initialise() {
        vehicles = new ArrayList<>();
        tracks = new ArrayList<>();
        blockades = new HashMap<>();
    }
}

package PackageRaymond;

import java.util.*;

public class RaymondSimulation {
    private final Map<Integer, Process> processes = new HashMap<>();
    private final Scanner scanner = new Scanner(System.in);

    public void setup() {
        System.out.println("=== Raymond's Algorithm Simulation ===");
        System.out.print("Enter number of processes: ");
        int n = scanner.nextInt();

        for (int i = 1; i <= n; i++) {
            processes.put(i, new Process(i, processes));
        }

        System.out.print("Enter initial token holder: ");
        int tokenHolder = scanner.nextInt();
        processes.get(tokenHolder).setToken(true);

        for (int i = 1; i <= n; i++) {
            if (i != tokenHolder) {
                System.out.print("Enter parent of Process " + i + ": ");
                int parent = scanner.nextInt();
                processes.get(i).setParent(parent);
            }
        }

        processes.values().forEach(Thread::start);

        System.out.println("\nInitial Setup Complete!");
        System.out.println("Token holder: Process " + tokenHolder);
        showStatus();
    }

    public void simulate() {
        int[] sampleRequests = {4, 1, 7}; // Simulate requests
        for (int pid : sampleRequests) {
            Process p = processes.get(pid);
            if (p != null) new Thread(p::requestCriticalSection).start();
            try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
        }

        try { Thread.sleep(5000); } catch (InterruptedException ignored) {}
        showStatus();
        processes.values().forEach(Process::stopProcess);
        System.out.println("Simulation ended.");
    }

    private void showStatus() {
        System.out.println("\n--- Current Status ---");
        processes.values().forEach(Process::printState);
        for (Process p : processes.values()) {
            if (p.hasToken()) System.out.println("*** Process " + p.getId() + " has TOKEN ***");
            if (p.isInCS()) System.out.println("*** Process " + p.getId() + " in CRITICAL SECTION ***");
        }
        System.out.println("--------------------");
    }

    public static void main(String[] args) {
        RaymondSimulation sim = new RaymondSimulation();
        sim.setup();
        sim.simulate();
    }
}

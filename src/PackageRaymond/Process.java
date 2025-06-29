package PackageRaymond;

import java.util.*;
import java.util.concurrent.*;

public class Process extends Thread {
    private final int id;
    private final Map<Integer, Process> allProcesses;
    private final BlockingQueue<Message> queue = new LinkedBlockingQueue<>();
    private final Queue<Integer> requestQueue = new LinkedList<>();
    private Integer parent;
    private boolean hasToken = false;
    private boolean requestSent = false;
    private boolean inCS = false;
    private boolean running = true;

    public Process(int id, Map<Integer, Process> allProcesses) {
        this.id = id;
        this.allProcesses = allProcesses;
        this.setName("Process-" + id);
    }

    public void setParent(Integer p) { this.parent = p; }
    public void setToken(boolean token) { this.hasToken = token; }

    public void requestCriticalSection() {
        log("wants to enter Critical Section");
        requestQueue.offer(id);
        if (parent != null && !requestSent) {
            requestSent = true;
            sendMessage(parent, new Message(Message.Type.REQUEST, id, id));
        }
        if (hasToken && !requestQueue.isEmpty() && requestQueue.peek() == id) {
            enterCriticalSection();
        }
    }

    public void receiveMessage(Message msg) {
        try {
            queue.put(msg);
        } catch (InterruptedException ignored) {}
    }

    private void sendMessage(int toId, Message msg) {
        Process target = allProcesses.get(toId);
        if (target != null) {
            target.receiveMessage(msg);
            log("sent " + msg.getType() + " to Process " + toId);
        }
    }

    private void enterCriticalSection() {
        requestQueue.poll();
        inCS = true;
        requestSent = false;
        log(">>> ENTERED CRITICAL SECTION <<<");
        try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
        exitCriticalSection();
    }

    private void exitCriticalSection() {
        inCS = false;
        log(">>> EXITED CRITICAL SECTION <<<");

        if (!requestQueue.isEmpty()) {
            int next = requestQueue.poll();
            hasToken = false;
            sendMessage(next, new Message(Message.Type.TOKEN, id, next));
            parent = next;
            if (!requestQueue.isEmpty()) {
                requestSent = true;
                sendMessage(parent, new Message(Message.Type.REQUEST, id, id));
            }
        }
    }

    private void handleRequest(Message msg) {
        int requesterId = msg.getRequesterId();
        log("received REQUEST from Process " + requesterId);
        requestQueue.offer(requesterId);
        if (parent != null && !requestSent) {
            requestSent = true;
            sendMessage(parent, new Message(Message.Type.REQUEST, id, id));
        }
        if (hasToken && !inCS) processToken();
    }

    private void handleToken(Message msg) {
        hasToken = true;
        requestSent = false;
        parent = null;
        log("received TOKEN from Process " + msg.getSenderId());
        processToken();
    }

    private void processToken() {
        if (!requestQueue.isEmpty() && requestQueue.peek() == id) {
            enterCriticalSection();
        } else if (!requestQueue.isEmpty()) {
            int next = requestQueue.poll();
            hasToken = false;
            sendMessage(next, new Message(Message.Type.TOKEN, id, next));
            parent = next;
            if (!requestQueue.isEmpty()) {
                requestSent = true;
                sendMessage(parent, new Message(Message.Type.REQUEST, id, id));
            }
        }
    }

    public void printState() {
        System.out.println("Process " + id + ": Parent=" + parent + ", HasToken=" + hasToken +
                ", RequestSent=" + requestSent + ", InCS=" + inCS + ", Queue=" + requestQueue);
    }

    private void log(String msg) {
        System.out.println("Process " + id + ": " + msg);
    }

    public void run() {
        while (running) {
            try {
                Message msg = queue.poll(1, TimeUnit.SECONDS);
                if (msg != null) {
                    if (msg.getType() == Message.Type.REQUEST) handleRequest(msg);
                    else if (msg.getType() == Message.Type.TOKEN) handleToken(msg);
                }
            } catch (InterruptedException ignored) {}
        }
    }

    public void stopProcess() {
        running = false;
        this.interrupt();
    }

    public boolean hasToken() { return hasToken; }
    public boolean isInCS() { return inCS; }
    public long getId() { return id; }
}

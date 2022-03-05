package com.company.pnrservices.core;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;
import com.google.common.util.concurrent.SimpleTimeLimiter;
import com.google.common.util.concurrent.TimeLimiter;

import java.io.*;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.lang.Thread.sleep;

public class ClientSSHMain {
    private final String ip;
    private final int port;
    private final String login;
    private final String password;
    private Session session = null;
    private Connection connect = null;
    private final String mac;
    public boolean isConnected = false;
    public boolean isAuth = false;

    public ClientSSHMain(String ip, int port, String login, String password, String mac) {
        this.ip = ip;
        this.port = port;
        this.login = login;
        this.password = password;
        this.mac = mac;
    }

    private List<String> sendCommandOA_(String command) {
        List<String> answer = null;
        session = runTunnel(ip, port, login, password, 5);
        if (session != null) {
            try {
                    session.execCommand(command);
                    answer = new ArrayList<>();
                    answer = tl_exec();
            } catch (IOException e) {
                System.out.println("!!!SSH-CLIENT: " + e.getMessage());
                e.printStackTrace();
            }
            finally {
                session.close();
                connect.close();
            }
        } else System.out.println("!!!session = null");
        return answer;
    }

    private List<String> tl_exec() {
        List<String> ret = new ArrayList<>();
         ExecutorService es = Executors.newSingleThreadExecutor();
        TimeLimiter tl = SimpleTimeLimiter.create(es);
        try {
            tl.callWithTimeout(() -> {
                InputStream stdout = new StreamGobbler(session.getStdout());
                BufferedReader br = new BufferedReader(new InputStreamReader(stdout, "UTF-8"));
                int repeat = 5;
                sleep(1000);
                while (!br.ready() && repeat > 0) {
                    repeat--;
                    System.out.println("!!!repeat = " + repeat + ", mac = " + mac);
                    sleep(500);
                }
                String line = br.readLine();
                while (line != null) {
                    ret.add(line);
                    line = br.readLine();
                }
                br.close();
                stdout.close();
                return null;
            }, 180L, TimeUnit.SECONDS);
            return ret;
        } catch (TimeoutException e) {
            System.out.println("!!!SSH CONNECT: TIMEOUT!: " + e.getMessage());
            e.printStackTrace();
        } catch (UncheckedIOException e) {
            System.out.println("!!!SSH CONNECT: TIMEOUT!: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("!!!SSH CONNECT: " + e.getMessage());
            e.printStackTrace();
        } finally {
            es.shutdown();
        }
        return null;
    }

    public List<String> sendCommandOA(String command) {
        return sendCommandOA_(command);
    }

    private Session runTunnel(String ip, int port, String name, String pass, int connectRepeat) {
        try {
            connect = new Connection(ip, port);
            while(connectRepeat > 0) {
                try {
                    connect.connect();
                } catch (Exception e) {
                    connectRepeat--;
                    try {
                        sleep(1000);
                    } catch (InterruptedException ignored){}
                    continue;
                }
                isConnected = true;
                break;
            }
            if (isConnected) {
                isAuth = connect.authenticateWithPassword(name, pass);
                if (!isAuth) {
                    throw new IOException("Auth failed.");
                }
                return connect.openSession();
            }
        } catch (UnknownHostException uhe) {
            System.out.println("!!!SSH-CLIENT: Uncorrect host: " + uhe.getMessage());
            uhe.printStackTrace();
        } catch (IOException e) {
            System.out.println("SSH-CLIENT: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean setConnect(String ip, int port, int connectRepeat) {
        connect = new Connection(ip, port);
        while(connectRepeat > 0) {
            try {
                connect.connect();
            } catch (Exception e) {
                connectRepeat--;
                System.out.println("repeat = "+connectRepeat + " !!!mac = " + mac);
                try {
                    sleep(1000);
                } catch (InterruptedException ignored){}
                continue;
            }
            isConnected = true;
            break;
        }
        return isConnected;
    }

    public Session setSession(String name, String pass) {
        if (isConnected && !isAuth) {
            if (!connect.isAuthenticationComplete()) {
                try {
                    isAuth = connect.authenticateWithPassword(name, pass);
                    if (!isAuth) {
                        throw new IOException("Auth failed.");
                    }
                    return connect.openSession();
                } catch (IOException e) {
                    System.out.println("!!!seSession exception: " + e.getMessage());
                    e.printStackTrace();
                }
            } else isAuth = true;
        }
        return null;
    }

//    public List<String> sendCommandMACList(List<String> macs) {
//        List<String> retList = new ArrayList<>();
//        if(macs.size() == 0) return null;
//        if (setConnect(ip, port, 5)) {
//            index = 0;
//            for (String mac : macs) {
//                index++;
//                System.out.println(index + " !!!mac = "+mac);
//                Session session = setSession(login, password);
//                if (isAuth) { //runTunnel(ip, port, login, password, 5);
//                    try {
//                        session.execCommand("topology.getMeterInfo " + mac);
//                        List<String> answer;
//                        answer = tl_exec(session);
//                        if (answer != null) {
//                            StringBuilder sb = new StringBuilder();
//                            for (String s : answer) {
//                                sb.append(s);
//                            }
//                            System.out.println("!!!mac = " + mac + ", answer = " + sb.toString());
//                            retList.add(mac + ";" + sb.toString());
//                        }
//                    } catch (IOException e) {
//                        System.out.println("!!!SSH-CLIENT: " + e.getMessage());
//                        e.printStackTrace();
//                    } finally {
//                        isAuth = false;
//                        //session.close();
//                    }
//                } else {
//                    System.out.println("!!!session = false");
//                    isAuth = false;
//                }
//            }
//        } else  System.out.println("!!!connect = false");
//        if (isConnected) {
//            isConnected = false;
//            isAuth = false;
//            connect.close();
//        }
//        return retList;
//    }


}

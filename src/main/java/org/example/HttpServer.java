package org.example;

import javax.print.DocFlavor;
import java.net.*;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;

public class HttpServer {
    public static HashMap<String, String> values = new HashMap<>();
    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = null;
        try {
            serverSocket = new ServerSocket(36000);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 35000.");
            System.exit(1);
        }
        while(true) {
            Socket clientSocket = null;
            try {
                System.out.println("Listo para recibir ...");
                clientSocket = serverSocket.accept();
            } catch (IOException e) {
                System.err.println("Accept failed.");
                System.exit(1);
            }
            PrintWriter out = new PrintWriter(
                    clientSocket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(clientSocket.getInputStream()));
            String inputLine, outputLine;
            boolean firstLine = true;
            String peticion = "";
            while ((inputLine = in.readLine()) != null) {
                if(firstLine){
                    if(inputLine.startsWith("GET")){
                        peticion = inputLine.split(" ")[1];
                    }
                }
                System.out.println("Recibí: " + inputLine);
                if (!in.ready()) {
                    break;
                }
            }
            try{
                handleRequest(peticion, out);
            }catch (Exception e){
                System.out.println(e.getMessage());
            }
            out.close();
            in.close();
            clientSocket.close();
        }
    }
    public static void handleRequest(String request, PrintWriter out){
        if(request.startsWith("/cliente")){
            out.println(getHTML());
        }else if(request.startsWith("/setkv")){
            if (request.split("=").length == 3){
                    out.println(setKV(request));
                }
        }else if(request.startsWith("/getkv")){
            if(request.split("=").length == 2){
                out.println(getKv(request));
            }

        }
    }
    public static String getHTML(){
        return "HTTP/1.1 200 OK\r\n"
                + "Content-Type: text/html\r\n"
                + "\r\n" +
                "<!DOCTYPE html>\n" +
                "<html>\n" +
                "\n" +
                "<head>\n" +
                "    <title>Form Example</title>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "</head>\n" +
                "\n" +
                "<body>\n" +
                "    <h1>Form with SET</h1>\n" +
                "    <form action=\"/hello\">\n" +
                "        <label for=\"name\">key:</label><br>\n" +
                "        <input type=\"text\" id=\"name\" name=\"name\" value=\"John\"><br><br>\n" +
                "        <label for=\"name\">value:</label><br>\n" +
                "        <input type=\"text\" id=\"name2\" name=\"name\" value=\"John\"><br><br>\n" +
                "        <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg()\">\n" +
                "    </form>\n" +
                "    <div id=\"getrespmsg\"></div>\n" +
                "    <h1>Form with GET</h1>\n" +
                "    <form action=\"/hello\">\n" +
                "        <label for=\"name\">key:</label><br>\n" +
                "        <input type=\"text\" id=\"name3\" name=\"name\" value=\"John\"><br><br>\n" +
                "        <input type=\"button\" value=\"Submit\" onclick=\"loadGetMsg2()\">\n" +
                "\n" +
                "    <div id=\"getrespmsg2\"></div>\n"+
                "    <script>\n" +
                "        function loadGetMsg() {\n" +
                "            let nameVar = document.getElementById(\"name\").value;\n" +
                "            let nameVar2 = document.getElementById(\"name2\").value;\n" +
                "            const xhttp = new XMLHttpRequest();\n" +
                "            xhttp.onload = function () {\n" +
                "                document.getElementById(\"getrespmsg\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "            }\n" +
                "            xhttp.open(\"GET\", \"/setkv?key=\" + nameVar + \"&value=\" + nameVar2);\n" +
                "            xhttp.send();\n" +
                "        }\n" +
                "        function loadGetMsg2() {\n" +
                "            let nameVar = document.getElementById(\"name3\").value;\n" +
                "            const xhttp = new XMLHttpRequest();\n" +
                "            xhttp.onload = function () {\n" +
                "                document.getElementById(\"getrespmsg2\").innerHTML =\n" +
                "                    this.responseText;\n" +
                "            }\n" +
                "            xhttp.open(\"GET\", \"/getkv?key=\" + nameVar);\n" +
                "            xhttp.send();\n" +
                "        }\n" +
                "    </script>\n" +
                "\n" +
                "</body>\n" +
                "\n" +
                "</html>";
    }
    public static String setKV(String peticion){
        System.out.println(peticion);
        try{
            String valor = peticion.split("=")[1].split("&")[0];
            System.out.println(valor);
            String valor2 = peticion.split("=")[2];
            System.out.println(valor2);
            String valorantiguo = values.get(valor);
            values.put(valor, valor2);
            if(valorantiguo== null){
                System.out.println("nuevo");
                return  "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n" + "{ \"key\": \"" + valor + "\", \"value\": \"" + valor2 + "\", \"status\": \"" + "creado" +"\"}" ;
            }else{
                return "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n" + "{ \"key\": \"" + valor + "\", \"value\": \"" + valor2 + "\", \"status\": \"" + "remplazado" +"\"}" ;
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }

        //System.out.println(Arrays.toString(peticion));
        //peticion.split("=")[1].split("\\{")[0].split("}")[0]
        return "Arrays.toString(peticion);";
    }
    public static String getKv(String peticion){
        try{
            String valor = peticion.split("=")[1];
            System.out.println(valor);
            String valorantiguo = values.get(valor);
            if(valorantiguo== null){
                System.out.println("no se encontro");
                String error = "key_not_found";
                return "HTTP/1.1 400 Not Found\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"+ "{ \"error\": \"" + error + "\", \"value\": \"" + valor + "\"}";

            }else{
                return "HTTP/1.1 200 OK\r\n"
                        + "Content-Type: application/json\r\n"
                        + "\r\n"+"{ \"key\": \"" + valor + "\", \"value\": \"" + valorantiguo + "\"}";
            }
        }catch(Exception e){
            System.out.println(e.getMessage());
        }
        //String hola = "{ \"key\": \"" + valor + "\"}";
        // String hola = "{ \"key\": \"mi_llave\", \"value\": \"mi_valor\" }";
        //System.out.println(Arrays.toString(peticion));
        //peticion.split("=")[1].split("\\{")[0].split("}")[0]
        return "Arrays.toString(peticion);";
    }
}

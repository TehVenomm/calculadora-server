//Trabalho Comunicacao entre processos - Sistemas Operacionais
//Gabriel Braz e Santos - 260569

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Main
{

    public static void main(String[] args) {
        Scanner scan = new Scanner(System.in);
        String addr_to_acionador;
        int port_to_calculadora;
        int port_to_acionador;
        String text1;
        String text2;
        String text3;
        String response;
        String resultado;
        int attempts = 0;
        double resultado_calculo;

        do
        {
            System.out.println("Insira a porta de envio de dados sentido Acionador -> Calculadora: (Ex: 8000)");
            port_to_calculadora = scan.nextInt();

            System.out.println("Insira a porta de envio de dados sentido Calculadora -> Acionador: (Ex: 8001)");
            port_to_acionador = scan.nextInt();

            if (port_to_calculadora == port_to_acionador) {
                System.out.println("As portas devem ser diferentes!");
            }
        } while (port_to_calculadora == port_to_acionador);

        try (ServerSocket serverSocket = new ServerSocket(port_to_calculadora)) {
            while (true) {
                System.out.println("------\nEsperando conexao do acionador na porta: " + port_to_calculadora);

                Socket socket = serverSocket.accept();
                addr_to_acionador = socket.getInetAddress().toString().split("/")[1];

                System.out.println("Acionador conectado! Endereco: " + addr_to_acionador);

                InputStream input = socket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));

                OutputStream output = socket.getOutputStream();
                PrintWriter writer = new PrintWriter(output, true);

                text1 = reader.readLine();
                response = "Recebido primeiro valor: " + text1;
                writer.println(response);
                System.out.println("------\n" + response);

                text2 = reader.readLine();
                response = "Recebido segundo valor: " + text2;
                writer.println(response);
                System.out.println(response);

                text3 = reader.readLine();
                response = "Recebido operacao: " + text3;
                writer.println(response);
                System.out.println(response);

                writer.println("Okay!");
                System.out.println("Conexao encerrada.");

                //Calculo
                System.out.println("------\nCalculando -> " + text1 + " " + text3 + " " + text2 + " = ?");
                resultado_calculo = 0;
                resultado = "";

                try
                {
                    switch (text3)
                    {
                        case "+":
                            resultado_calculo = Integer.parseInt(text1) + Integer.parseInt(text2);
                            break;
                        case "-":
                            resultado_calculo = Integer.parseInt(text1) - Integer.parseInt(text2);
                            break;
                        case "/":
                            resultado_calculo = Integer.parseInt(text1) / Integer.parseInt(text2);
                            break;
                        case "*":
                            resultado_calculo = Integer.parseInt(text1) * Integer.parseInt(text2);
                            break;
                        default:
                            System.out.println("Operacao invalida! Impossivel realizar calculo.");
                            resultado = "Operacao Invalida!";
                            break;
                    }

                    if (resultado.isEmpty()){
                        resultado = text1 + " " + text3 + " " + text2 + " = " + resultado_calculo;
                    }
                } catch (NumberFormatException e){
                    resultado = "Valores Invalidos!";
                    System.out.println("Numeros invalidos! Impossivel realizar calculo.");
                }

                //Abre socket para enviar dados para a calculadora
                do
                {
                    System.out.println("------\nConectando com acionador em: " + addr_to_acionador + ":" + port_to_acionador + "...");
                    try (Socket socket_to_acionador = new Socket(addr_to_acionador, port_to_acionador))
                    {
                        System.out.println("Acionador conectado em: " + addr_to_acionador + ":" + port_to_acionador);

                        //Abre steam de input e output de dados dentro do socket
                        OutputStream output_to_acionador = socket_to_acionador.getOutputStream();
                        PrintWriter writer_to_acionador = new PrintWriter(output_to_acionador, true);

                        InputStream input_from_acionador = socket_to_acionador.getInputStream();
                        BufferedReader reader_from_acionador = new BufferedReader(new InputStreamReader(input_from_acionador));

                        //Envio do resultado
                        writer_to_acionador.println(resultado);
                        System.out.println("Enviado!");
                        String response_from_acionador = reader_from_acionador.readLine();
                        System.out.println("------\nResposta acionador (" + response_from_acionador + ")");

                        response_from_acionador = reader_from_acionador.readLine();
                        System.out.println("Resposta acionador (" + response_from_acionador + ")");

                        socket.close();
                        attempts = 11;
                        System.out.println("------\nConexao encerrada com acionador - Voltando para servidor calculadora.");
                    } catch (UnknownHostException ex)
                    {
                        System.out.println("Endereco nao encontrado: " + ex.getMessage());
                        attempts++;

                        if (attempts <= 10) {
                            try
                            {
                                System.out.println("Re-tentativa em 5 segundos... (" + attempts + "/10)");
                                Thread.sleep(5000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Nao foi possivel conectar, desistindo de conectar com acionador.");
                        }
                    } catch (IOException ex)
                    {
                        System.out.println("Erro - " + ex.getMessage());
                        attempts++;

                        if (attempts <= 10) {
                            try
                            {
                                System.out.println("Re-tentativa em 5 segundos... (" + attempts + "/10)");
                                Thread.sleep(5000);
                            } catch (InterruptedException e)
                            {
                                e.printStackTrace();
                            }
                        } else {
                            System.out.println("Nao foi possivel conectar, desistindo de conectar com acionador.");
                        }

                    }
                } while (attempts < 10);

                socket.close();
            }

        } catch (IOException ex) {
            System.out.println("Erro - " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
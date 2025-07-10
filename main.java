// Importa a biblioteca Gson para trabalhar com JSON
import com.google.gson.Gson;

// Importa classes para manipulação de entrada/saída
import java.io.IOException;

// Importa classes para manipular URIs e chamadas HTTP
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

// Importa para formatar números decimais
import java.text.DecimalFormat;

// Importa para ler entrada do usuário
import java.util.Scanner;

public class Main {

    // Chave da API para autenticação — substitua por sua chave real
    private static final String API_KEY = "sua_chave_api";

    // URL base da API de câmbio
    private static final String BASE_URL = "https://api.exchangerate-api.com/v4/latest/";

    // Formato para exibir o resultado com duas casas decimais
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Cria objeto para ler entrada do usuário

        while (true) {
            exibirMenu(); // Mostra o menu de opções
            int opcao = scanner.nextInt(); // Lê a opção do usuário

            if (opcao == 0) { // Verifica se o usuário quer sair
                System.out.println("Programa encerrado. Até mais!");
                break; // Sai do loop
            }

            if (opcao < 1 || opcao > 6) { // Valida se a opção é válida
                System.out.println("Opção inválida! Tente novamente.");
                continue; // Volta ao menu
            }

            // Solicita o valor para conversão
            System.out.print("Digite o valor a ser convertido: ");
            double valor = scanner.nextDouble();

            try {
                // Chama a função para conversão e armazena o resultado
                double resultado = converterMoeda(opcao, valor);

                // Gera uma mensagem formatada com o resultado da conversão
                String mensagem = gerarMensagemResultado(opcao, valor, resultado);

                // Exibe a mensagem final
                System.out.println(mensagem);
            } catch (IOException | InterruptedException e) {
                // Trata erro de comunicação com a API
                System.out.println("Erro ao acessar a API: " + e.getMessage());
            }
        }

        scanner.close(); // Fecha o scanner após sair do loop
    }

    // Exibe o menu de opções para o usuário
    private static void exibirMenu() {
        System.out.println("\n=== CONVERSOR DE MOEDAS ===");
        System.out.println("1 - Dólar (USD) → Real (BRL)");
        System.out.println("2 - Real (BRL) → Dólar (USD)");
        System.out.println("3 - Euro (EUR) → Real (BRL)");
        System.out.println("4 - Real (BRL) → Euro (EUR)");
        System.out.println("5 - Libra (GBP) → Real (BRL)");
        System.out.println("6 - Real (BRL) → Libra (GBP)");
        System.out.println("0 - Sair");
        System.out.print("Escolha uma opção: ");
    }

    // Realiza a conversão da moeda com base na opção escolhida
    private static double converterMoeda(int opcao, double valor) throws IOException, InterruptedException {
        String moedaOrigem, moedaDestino;

        // Define as moedas de origem e destino com base na opção escolhida
        switch (opcao) {
            case 1: moedaOrigem = "USD"; moedaDestino = "BRL"; break;
            case 2: moedaOrigem = "BRL"; moedaDestino = "USD"; break;
            case 3: moedaOrigem = "EUR"; moedaDestino = "BRL"; break;
            case 4: moedaOrigem = "BRL"; moedaDestino = "EUR"; break;
            case 5: moedaOrigem = "GBP"; moedaDestino = "BRL"; break;
            case 6: moedaOrigem = "BRL"; moedaDestino = "GBP"; break;
            default: throw new IllegalArgumentException("Opção inválida");
        }

        // Cria um cliente HTTP para enviar a requisição
        HttpClient client = HttpClient.newHttpClient();

        // Cria a requisição HTTP com URI baseada na moeda de origem
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + moedaOrigem)) // Monta a URL da API
                .header("Authorization", "Bearer " + API_KEY) // Adiciona a chave da API (caso necessário)
                .build();

        // Envia a requisição e armazena a resposta como string
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Cria um objeto Gson para converter JSON em objeto Java
        Gson gson = new Gson();

        // Converte o JSON da resposta para um objeto ExchangeRateResponse
        ExchangeRateResponse rateResponse = gson.fromJson(response.body(), ExchangeRateResponse.class);

        // Obtém a taxa de câmbio da moeda de destino
        double taxa = rateResponse.getRates().get(moedaDestino);

        // Retorna o valor convertido
        return valor * taxa;
    }

    // Gera uma string formatada com o resultado da conversão
    private static String gerarMensagemResultado(int opcao, double valor, double resultado) {
        // Define o nome das conversões disponíveis
        String[] moedas = {"", "USD → BRL", "BRL → USD", "EUR → BRL", "BRL → EUR", "GBP → BRL", "BRL → GBP"};

        // Usa formatação para montar a string com o valor original, moeda e resultado
        return String.format("\n%.2f %s = %s %s", 
                valor, 
                moedas[opcao].split("→")[0].trim(), 
                df.format(resultado), 
                moedas[opcao].split("→")[1].trim());
    }

    // Classe interna que representa a resposta da API
    static class ExchangeRateResponse {
        private String base;  // Moeda base
        private String date;  // Data da taxa
        private Rates rates;  // Objeto com as taxas de câmbio

        // Getter para acessar as taxas
        public Rates getRates() {
            return rates;
        }
    }

    // Classe interna que representa as taxas de câmbio
    static class Rates {
        private double USD;
        private double BRL;
        private double EUR;
        private double GBP;

        // Método que retorna a taxa da moeda especificada
        public double get(String currency) {
            switch (currency) {
                case "USD": return USD;
                case "BRL": return BRL;
                case "EUR": return EUR;
                case "GBP": return GBP;
                default: return 0; // Retorna 0 se a moeda não for encontrada
            }
        }
    }
}

import com.google.gson.Gson; // Biblioteca para manipulação de JSON
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.DecimalFormat;
import java.util.Scanner;

public class Main {
    // Chave da API fornecida — substitua com segurança em produção!
    private static final String API_KEY = "7a17cc3246ed5438530ba205";

    // Base da URL da API ExchangeRate API versão 6
    private static final String BASE_URL = "https://v6.exchangerate-api.com/v6/";

    // Formata valores com duas casas decimais
    private static final DecimalFormat df = new DecimalFormat("0.00");

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in); // Entrada de dados do usuário

        while (true) {
            exibirMenu(); // Mostra as opções disponíveis
            int opcao = scanner.nextInt(); // Lê a opção escolhida

            if (opcao == 0) { // Caso o usuário queira sair
                System.out.println("Programa encerrado. Até mais!");
                break;
            }

            if (opcao < 1 || opcao > 6) { // Validação de entrada
                System.out.println("Opção inválida! Tente novamente.");
                continue;
            }

            System.out.print("Digite o valor a ser convertido: ");
            double valor = scanner.nextDouble(); // Valor que será convertido

            try {
                // Realiza a conversão
                double resultado = converterMoeda(opcao, valor);

                // Cria e exibe a mensagem formatada com o resultado
                String mensagem = gerarMensagemResultado(opcao, valor, resultado);
                System.out.println(mensagem);
            } catch (IOException | InterruptedException e) {
                // Trata erros de conexão com a API
                System.out.println("Erro ao acessar a API: " + e.getMessage());
            }
        }

        scanner.close(); // Fecha o Scanner ao final
    }

    // Exibe as opções de conversão
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

    // Realiza a chamada HTTP para buscar a taxa de câmbio e converter o valor
    private static double converterMoeda(int opcao, double valor) throws IOException, InterruptedException {
        String moedaOrigem, moedaDestino;

        // Define moeda de origem e destino com base na opção escolhida
        switch (opcao) {
            case 1: moedaOrigem = "USD"; moedaDestino = "BRL"; break;
            case 2: moedaOrigem = "BRL"; moedaDestino = "USD"; break;
            case 3: moedaOrigem = "EUR"; moedaDestino = "BRL"; break;
            case 4: moedaOrigem = "BRL"; moedaDestino = "EUR"; break;
            case 5: moedaOrigem = "GBP"; moedaDestino = "BRL"; break;
            case 6: moedaOrigem = "BRL"; moedaDestino = "GBP"; break;
            default: throw new IllegalArgumentException("Opção inválida");
        }

        // Monta a URL da requisição conforme a API da ExchangeRate
        String url = BASE_URL + API_KEY + "/latest/" + moedaOrigem;

        // Cria um cliente HTTP
        HttpClient client = HttpClient.newHttpClient();

        // Prepara a requisição GET
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .build();

        // Envia a requisição e obtém a resposta como string
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Converte o JSON da resposta para o objeto Java
        Gson gson = new Gson();
        ExchangeRateResponse rateResponse = gson.fromJson(response.body(), ExchangeRateResponse.class);

        // Obtém a taxa da moeda desejada
        double taxa = rateResponse.getConversionRates().get(moedaDestino);

        // Retorna o valor convertido
        return valor * taxa;
    }

    // Gera uma mensagem com o resultado da conversão
    private static String gerarMensagemResultado(int opcao, double valor, double resultado) {
        String[] moedas = {"", "USD → BRL", "BRL → USD", "EUR → BRL", "BRL → EUR", "GBP → BRL", "BRL → GBP"};
        return String.format("\n%.2f %s = %s %s", 
                valor, 
                moedas[opcao].split("→")[0].trim(), 
                df.format(resultado), 
                moedas[opcao].split("→")[1].trim());
    }

    // Classe que representa a resposta da API
    static class ExchangeRateResponse {
        private String base_code; // Código da moeda base
        private java.util.Map<String, Double> conversion_rates; // Mapa com as taxas de câmbio

        public java.util.Map<String, Double> getConversionRates() {
            return conversion_rates;
        }
    }
}

package view;

import config.Mensagens;
import controller.CarteiraController;
import dto.CarteiraDTO;
import exception.AppException;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Cadastro de carteiras: inclusão, consulta, edição e exclusão.
 */
public class MenuCarteira extends Menu {

    private final CarteiraController controller;

    public MenuCarteira(Console console, CarteiraController controller) {
        super(console, Mensagens.get("carteira.titulo"));
        this.controller = controller;
        adicionarOpcao(Mensagens.get("carteira.incluir"), this::incluirCarteira);
        adicionarOpcao(Mensagens.get("carteira.listar"), this::listarCarteiras);
        adicionarOpcao(Mensagens.get("carteira.editar"), this::editarCarteira);
        adicionarOpcao(Mensagens.get("carteira.excluir"), this::excluirCarteira);
    }

    private void incluirCarteira() throws AppException {
        String nomeTitular = console.lerLinha(Mensagens.get("carteira.pedirTitular"));
        String corretora = console.lerLinha(Mensagens.get("carteira.pedirCorretora"));
        CarteiraDTO carteira = controller.incluir(nomeTitular, corretora);
        console.exibir(Mensagens.get("carteira.incluida", Formatador.carteira(carteira)));
    }

    private void listarCarteiras() throws AppException {
        List<CarteiraDTO> carteiras = controller.listarTodas();
        if (carteiras.isEmpty()) {
            console.exibir(Mensagens.get("relatorios.semCarteiras"));
            return;
        }
        carteiras.sort(Comparator.comparing(CarteiraDTO::getNomeTitular, String.CASE_INSENSITIVE_ORDER));
        console.exibir(Mensagens.get("carteira.cadastradas"));
        for (CarteiraDTO carteira : carteiras) {
            console.exibir("  " + Formatador.carteira(carteira));
        }
    }

    private void editarCarteira() throws AppException {
        Optional<CarteiraDTO> escolhida = escolherCarteira(controller.listarTodas());
        if (escolhida.isEmpty()) {
            return;
        }
        int identificador = escolhida.get().getIdentificador();
        console.exibir(Mensagens.get("carteira.atual", Formatador.carteira(escolhida.get())));

        String nomeTitular = console.lerLinha(Mensagens.get("carteira.pedirNovoTitular"));
        String corretora = console.lerLinha(Mensagens.get("carteira.pedirNovaCorretora"));
        CarteiraDTO atualizada = controller.editar(identificador, nomeTitular, corretora);
        console.exibir(Mensagens.get("carteira.atualizada", Formatador.carteira(atualizada)));
    }

    private void excluirCarteira() throws AppException {
        Optional<CarteiraDTO> escolhida = escolherCarteira(controller.listarTodas());
        if (escolhida.isEmpty()) {
            return;
        }
        int identificador = escolhida.get().getIdentificador();
        console.exibir(Mensagens.get("carteira.aExcluir", Formatador.carteira(escolhida.get())));

        if (console.confirmar(Mensagens.get("carteira.confirmarExclusao"))) {
            controller.excluir(identificador);
            console.exibir(Mensagens.get("carteira.excluida"));
        } else {
            console.exibir(Mensagens.get("carteira.exclusaoCancelada"));
        }
    }
}

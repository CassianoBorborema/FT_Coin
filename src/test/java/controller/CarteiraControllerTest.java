package controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import dao.CarteiraDAO;
import dao.MovimentacaoDAO;
import dao.Persistencia;
import dto.CarteiraDTO;
import dto.MovimentacaoDTO;
import exception.AppException;
import java.time.LocalDate;
import model.TipoMovimentacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CarteiraControllerTest {

    private CarteiraDAO carteiraDAO;
    private MovimentacaoDAO movimentacaoDAO;
    private CarteiraController controller;

    @BeforeEach
    void setUp() {
        carteiraDAO = Persistencia.emMemoria().carteiras();
        movimentacaoDAO = Persistencia.emMemoria().movimentacoes();
        controller = new CarteiraController(carteiraDAO, movimentacaoDAO);
    }

    @Test
    void incluirGeraIdentificadorSequencial() throws AppException {
        assertEquals(1, controller.incluir("Ana", "XP").getIdentificador());
        assertEquals(2, controller.incluir("Bruno", "Rico").getIdentificador());
    }

    @Test
    void incluirExigeTitularECorretora() {
        assertThrows(AppException.class, () -> controller.incluir(" ", "XP"));
        assertThrows(AppException.class, () -> controller.incluir("Ana", null));
    }

    @Test
    void identificadorLiberadoEhReaproveitado() throws AppException {
        controller.incluir("Ana", "XP");
        controller.incluir("Bruno", "Rico");
        controller.excluir(1);

        assertEquals(1, controller.incluir("Carla", "BTG").getIdentificador());
    }

    @Test
    void editarAtualizaDados() throws AppException {
        controller.incluir("Ana", "XP");

        CarteiraDTO editada = controller.editar(1, "Ana Maria", "BTG");

        assertEquals("Ana Maria", editada.getNomeTitular());
        assertEquals("BTG", controller.consultar(1).getCorretora());
    }

    @Test
    void consultarCarteiraInexistenteFalha() {
        assertThrows(AppException.class, () -> controller.consultar(42));
        assertThrows(AppException.class, () -> controller.consultar(0));
    }

    @Test
    void excluirRemoveCarteiraSemMovimentacoes() throws AppException {
        controller.incluir("Ana", "XP");

        controller.excluir(1);

        assertFalse(carteiraDAO.existe(1));
    }

    @Test
    void excluirCarteiraComMovimentacoesEhBloqueado() throws AppException {
        controller.incluir("Ana", "XP");
        movimentacaoDAO.incluir(new MovimentacaoDTO(0, 1, LocalDate.now(), TipoMovimentacao.COMPRA, 10));

        assertThrows(AppException.class, () -> controller.excluir(1));
    }
}

package dao;

class PersistenciaMemoriaTest extends PersistenciaContrato {

    @Override
    protected Persistencia criarPersistenciaVazia() {
        return Persistencia.emMemoria();
    }
}

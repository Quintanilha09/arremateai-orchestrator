package com.arremateai.orchestrator.exception;

public class ServicoIndisponivelException extends RuntimeException {

    private final String nomeServico;

    public ServicoIndisponivelException(String nomeServico) {
        super("Serviço indisponível: " + nomeServico);
        this.nomeServico = nomeServico;
    }

    public ServicoIndisponivelException(String nomeServico, Throwable causa) {
        super("Serviço indisponível: " + nomeServico, causa);
        this.nomeServico = nomeServico;
    }

    public String getNomeServico() {
        return nomeServico;
    }
}

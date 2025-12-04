package ubi.pdm.fastravel.frontend.routes;

public class Route {
    private String origem;
    private String destino;
    private String transporte;
    private String data;

    public Route(String origem, String destino, String transporte, String data) {
        this.origem = origem;
        this.destino = destino;
        this.transporte = transporte;
        this.data = data;
    }

    public String getOrigem() {
        return origem;
    }

    public void setOrigem(String origem) {
        this.origem = origem;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    public String getTransporte() {
        return transporte;
    }

    public void setTransporte(String transporte) {
        this.transporte = transporte;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
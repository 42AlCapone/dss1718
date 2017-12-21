package main.business;

import java.sql.Date;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import main.data.DocenteDAO;
import main.data.AlunoDAO;
import main.data.AulaDAO;
import main.data.CadeiraDAO;
import main.data.DirecaoCursoDAO;
import main.data.TurnoDAO;
import main.data.TrocaDAO;

/**
 * Facade para a camada de business.
 * Contém os métodos identificados nos diagramas de sequencia de implementação
 * @author ruicouto
 */
public class GestorTurnos {
    //O docente com login actualmente
    private Utilizador sessao;
    
    private AlunoDAO alunosDAO;
    private AulaDAO aulasDAO;
    private CadeiraDAO cadeirasDAO;
    private DocenteDAO docentesDAO;
    private TurnoDAO turnosDAO;
    private TrocaDAO trocasDAO;
    private DirecaoCursoDAO direcaoCursoDAO;
   
    public GestorTurnos() {
        sessao = null; //deveria ser carregado da base de dados
        alunosDAO = new AlunoDAO();
        aulasDAO = new AulaDAO();
        cadeirasDAO = new CadeiraDAO();
        docentesDAO = new DocenteDAO();
        turnosDAO = new TurnoDAO();
        trocasDAO = new TrocaDAO();
        direcaoCursoDAO = new DirecaoCursoDAO();
    }

    public Utilizador getSessao() {
        return sessao;
    }

    public void iniciarSessao(String numero, String password) throws NumeroException{
        Utilizador u;
        try{
            if(direcaoCursoDAO.containsKey(numero)) 
                u = direcaoCursoDAO.get(numero);
            
            else if(docentesDAO.containsKey(numero)) 
                u = docentesDAO.get(numero);  
            
            else u = alunosDAO.get(numero);
            verificarPassword(u,password);
            sessao = u;
        }
        catch(Exception e){
            throw new NumeroException(e.getMessage());
        }
    }
    
    public void verificarPassword(Utilizador u, String password) throws PasswordException {
        if (u.getPassword().equals(password)) return;
        else throw new PasswordException("A password está errada");
    }
   
    public void adicionarAluno(String numero,String nome,String email,String password,int estatuto){
        Aluno a = new Aluno(numero,nome,email,password,estatuto);
        alunosDAO.put(a.getNumero(),a);
    }
    
    public void adicionarCadeira(Cadeira a){
        cadeirasDAO.put(a.getAcron(), a);
    }
    
    public void adicionarCadeira(String a, String b) {
        cadeirasDAO.put(a, new Cadeira(a,b));
    }
    
    public void adicionarDocente(String numero, String nome, String email, String password) {
        Docente d = new Docente(numero,nome,email,password);
        docentesDAO.put(numero,d);
    }

    public Set<String> getTurnos() {
        return turnosDAO.keySet();
    }

    public void adicionarAula(String data, String turno) {
        Aula a = new Aula(data,turno);
        aulasDAO.put(null,a);
    }
    
    public Map<String,String> getTurnosAluno() {
        String key = sessao.getNumero();
        return turnosDAO.getA(key);
    }
    
    public void adicionarTurno(Turno a){
        turnosDAO.put(a.getID(), a);
    }

    public List<String> getCadeirasAluno() {
        String key = sessao.getNumero();
        return cadeirasDAO.getA(key);
    }

    public int adicionarTroca(String t1,String t2) {
        String u1=sessao.getNumero();
        String u2 = trocasDAO.getAlunoTroca(t1,t2);
        if(u2==null) {
            trocasDAO.put("key", new Troca(0,t2,sessao.getNumero()));
            return 0;
        }
        trocasDAO.remove(u2,t1);
        turnosDAO.updateTurnoAluno(u1,t1,t2);
        turnosDAO.updateTurnoAluno(u2,t2,t1);
        return 1;
    }

    public List<String> getTurnosUC(String uc) {
        return turnosDAO.values().stream()
                                    .filter(u -> u.getIdUC().equals(uc))
                                    .map(u -> u.getID())
                                    .collect(Collectors.toList());
    }

    public Collection<Cadeira> getCadeiras() {
        return cadeirasDAO.values();
        
    }

    public Turno getTurno(String turno) {
        return turnosDAO.get(turno);
    }

    public void removerTurno(String turno) {
        turnosDAO.remove(turno);
    }
    
    public void gerarTurnos() {
        List<String> horario = new ArrayList<>();
        List<String> t = new ArrayList<>();
        List<String> p = new ArrayList<>();
        int n;
        for(Aluno a : alunosDAO.values()){
            for(String uc : a.getUcs()){
                for(String s: turnosDAO.getC(uc)){
                if (verificarTipo(s)==1) t.add(s);
                else p.add(s);
            }
            n=nTurnos(1,t);
            atribuiTurnos(t,horario,n);
            n+=nTurnos(2,p);
            atribuiTurnos(p,horario,n);
            }
    
        }
    }

     

    
    public void atribuiTurnos(List<String> turnos,List<String> horario,int n) {
        if (horario.size()==n) return;
        if (turnos.isEmpty()) {horario.clear();return;}

        for (int i=0;i<turnos.size();i++) {
                String t = turnos.get(i);
                if(nAulas(t)==2) {
                    horario.add(t);
                    horario.add(turnos.get(i+1));
                }
                else {
                    horario.add(t);
                }
                atribuiTurnos(deleteRep(turnos,horario),horario,n);
            }
        }
    
    
    
    private int nAulas(String turno) {
        String[] split = turno.split("-");
        for(String s:split) if(s.equals("A")) return 2;
        return 1;
    }
    
    public List<String> deleteRep(List<String> turnos,List<String> horario) {
        List<String> turnosAux = new ArrayList<>();
            
        for(String s1 : turnos) {
            Turno t1 = turnosDAO.get(s1);
            for(String s2 : horario){
                Turno t2 = turnosDAO.get(s2);
                if(t1.coincide(t2)==1) turnosAux.add(s1);
            }
        }
        return turnosAux;
    }

    private int verificarTipo(String turnoAtual) {
        String[] split = turnoAtual.split("-");
        int n;
        for(n=0;!split[n].isEmpty();n++){
            if(temNumeros(split[n])) break;
        }
        if (split[n].startsWith("T")) return 1;
        else return 2;
    }
    
    private Boolean temNumeros(String arg) {
        String[] nums = {"0","1","2","3","4","5","6","7","8","9"};
        for(int n=0;n<10;n++){
            if(arg.endsWith(nums[n])) return true;
        }
        return false;
    }

    private int nTurnos(int i, List<String> turnos) {
            int n=0;
            int ret=0;
            String [] split;
            if(n==1){
                for(String turno : turnos) {
                    split = turno.split("-");
                    for(n=0;!split[n].isEmpty();n++){
                        if(split[n].equals("T1")) {
                            if(split[n+1].equals("A")) ret+=2;
                            else ret+=1;
                        }
                    }
                }
            } else {
                for(String turno : turnos) {
                    split = turno.split("-");
                    for(n=0;!split[n].isEmpty();n++){
                        if(split[n].equals("P1")) {
                            if(split[n+1].equals("A")) ret+=2;
                            else ret+=1;
                        }
                    }
                }
            }
            return ret;
    }
}
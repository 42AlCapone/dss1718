/*
 * DirecaoCursoDAO.java
 *
 * Created on 23 de Maio de 2005, 18:23
 */

package main.data;

/**
 Tabela a criar em MYSQL:
 * 
 CREATE TABLE `docente` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `nome` varchar(45) DEFAULT NULL,
  `email` varchar(45) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `id_UNIQUE` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

* 
 * @author jfc
 */

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import main.business.DirecaoCurso;

public class DirecaoCursoDAO implements Map<String,DirecaoCurso> {
    
    private Connection conn;
    
    /**
     * Apagar todos os registos da direção de curso 
     */
    @Override
    public void clear () {
        try {
            conn = Connect.connect();
            Statement stm = conn.createStatement();
            stm.executeUpdate("DELETE * FROM direcaoCurso");
        } catch (Exception e) {
            //runtime exeption!
            throw new NullPointerException(e.getMessage()); 
        } finally {
            Connect.close(conn);
        }
    }
    
    /**
     * Verifica se o email de diretor de curso existe na base de dados
     * @param key
     * @return
     * @throws NullPointerException 
     */
    @Override
    public boolean containsKey(Object key) throws NullPointerException {
        boolean r = false;
        try {
            conn = Connect.connect();
            String sql = "SELECT `email` FROM `direcaoCurso` WHERE `email`=?;";
            PreparedStatement stm = conn.prepareStatement(sql);
            stm.setString(1, key.toString());
            ResultSet rs = stm.executeQuery();
            r = rs.next();
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        } finally {
            Connect.close(conn);
        }
        return r;
    }
    
    /**
     * Verifica se o diretor de curso existe na base de dados
     * 
     * Esta implementação é provisória. Devia testar todo o objecto e não apenas a chave.
     * 
     * @param value
     * @return 
     */
    @Override
    public boolean containsValue(Object value) {
        DirecaoCurso a = (DirecaoCurso) value;
        return containsKey(a.getEmail());
    }
    
    @Override
    public Set<Map.Entry<String,DirecaoCurso>> entrySet() {
        throw new NullPointerException("public Set<Map.Entry<String,DirecaoCurso>> entrySet() not implemented!");
    }
    
    @Override
    public boolean equals(Object o) {
        throw new NullPointerException("public boolean equals(Object o) not implemented!");
    }
    
    /**
     * Obter um diretor de curso, dado o seu email
     * @param key
     * @return 
     */
    @Override
    public DirecaoCurso get(Object key) {
        DirecaoCurso al = null;
        try {
            conn = Connect.connect();
            PreparedStatement stm = conn.prepareStatement("SELECT * FROM direcaoCurso WHERE email=?");
            stm.setString(1,(String)key);
            ResultSet rs = stm.executeQuery();
            if (rs.next()) 
                   al = new DirecaoCurso(rs.getString("nome"),rs.getString("email"),rs.getString("password"));
            
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Connect.close(conn);
        }
        return al;
    }
    
    @Override
    public int hashCode() {
        return this.conn.hashCode();
    }
    
    /**
     * Verifica se existem entradas
     * @return 
     */
    @Override
    public boolean isEmpty() {
        return size() == 0;
    }
    
    @Override
    public Set<String> keySet() {
        throw new NullPointerException("Not implemented!");
    }
    
    /**
     * Insere um diretor de curso na base de dados
     * @param key
     * @param value
     * @return 
     */
    @Override
    public DirecaoCurso put(String key, DirecaoCurso value) {
        DirecaoCurso al = null;
        try {
            conn = Connect.connect();
            PreparedStatement stm = conn.prepareStatement("INSERT INTO docente\n" +
                "VALUES (?, ?)\n" +
                "ON DUPLICATE KEY UPDATE nome = VALUES(nome),password=VALUES(password)", Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, value.getNome());
            stm.setString(2, value.getEmail());
            stm.setString(3,value.getPassword());
            stm.executeUpdate();
            
            ResultSet rs = stm.getGeneratedKeys();
            if(rs.next()) {
                String newId = rs.getString(1);
                value.setEmail(newId);
            }
            
            al = value;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Connect.close(conn);
        }
        return al;
    }

    /**
     * Por um conjunto de diretores de curso na base de dados
     * @param t 
     */
    @Override
    public void putAll(Map<? extends String,? extends DirecaoCurso> t) {
        for(DirecaoCurso a : t.values()) {
            put(a.getEmail(), a);
        }
    }
    
    /**
     * Remover um docente, dado o seu email
     * @param key
     * @return 
     */
    @Override
    public DirecaoCurso remove(Object key) {
        DirecaoCurso al = this.get(key);
        try {
            conn = Connect.connect();
            PreparedStatement stm = conn.prepareStatement("delete from direcaoCurso where email = ?");
            stm.setString(1, (String)key);
            stm.executeUpdate();
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        } finally {
            Connect.close(conn);
        }
        return al;
    }
    
    /**
     * Retorna o número de entradas na base de dados
     * @return 
     */
    @Override
    public int size() {
        int i = 0;
        try {
            conn = Connect.connect();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery("SELECT count(*) FROM direcaoCurso");
            if(rs.next()) {
                i = rs.getInt(1);
            }
        }
        catch (Exception e) {throw new NullPointerException(e.getMessage());}
        finally {
            Connect.close(conn);
        }
        return i;
    }
    
    /**
     * Obtém todos os diretores de curso da base de dados
     * @return 
     */
    @Override
    public Collection<DirecaoCurso> values() {
        Collection<DirecaoCurso> col = new HashSet<DirecaoCurso>();
        try {
            conn = Connect.connect();
            Statement stm = conn.createStatement();
            ResultSet rs = stm.executeQuery("SELECT * FROM direcaoCurso");
            while (rs.next()) {
                col.add(new DirecaoCurso(rs.getString("nome"),rs.getString("email"),rs.getString("password")));
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            Connect.close(conn);
        }
        return col;
    }
    
}
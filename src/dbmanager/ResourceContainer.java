//$Id$
package dbmanager;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;


public class ResourceContainer {
    private PreparedStatement preparedStatement;
    private CallableStatement callableStatement;
    private Connection connection;
    private Statement statement;
    private ResultSet res;
    
    public ResultSet getRes() {
		return res;
	}

	public void setRes(ResultSet res) {
		this.res = res;
	}

	public void setPreparedStatement(PreparedStatement preparedStatement) {
		this.preparedStatement = preparedStatement;
	}

	public void setCallableStatement(CallableStatement callableStatement) {
		this.callableStatement = callableStatement;
	}

	public void setConnection(Connection connection) {
		this.connection = connection;
	}

	public void setStatement(Statement statement) {
		this.statement = statement;
	}

	public ResourceContainer() {
    	
    }

    public ResourceContainer(PreparedStatement preparedStatement, CallableStatement callableStatement,
                             Connection connection, Statement statement, ResultSet res) {
        this.preparedStatement = preparedStatement;
        this.callableStatement = callableStatement;
        this.connection = connection;
        this.statement = statement;
        this.res = res;
    }

    public PreparedStatement getPreparedStatement() {
        return preparedStatement;
    }

    public CallableStatement getCallableStatement() {
        return callableStatement;
    }

    public Connection getConnection() {
        return connection;
    }

    public Statement getStatement() {
        return statement;
    }
    
    public ResultSet getResultSet() {
    	return res;
    }
}
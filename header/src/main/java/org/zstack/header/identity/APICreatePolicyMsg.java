package org.zstack.header.identity;

import org.zstack.header.identity.PolicyInventory.Statement;
import org.zstack.header.message.APICreateMessage;
import org.zstack.header.message.APIParam;

import java.util.List;

@Action(category = AccountConstant.ACTION_CATEGORY, accountOnly = true)
public class APICreatePolicyMsg extends APICreateMessage implements AccountMessage {
    @APIParam(maxLength = 255)
    private String name;
    @APIParam(maxLength = 2048, required = false)
    private String description;
    @APIParam(nonempty = true)
    private List<Statement> statements;

    public List<Statement> getStatements() {
        return statements;
    }

    public void setStatements(List<Statement> statements) {
        this.statements = statements;
    }

    @Override
    public String getAccountUuid() {
        return this.getSession().getAccountUuid();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

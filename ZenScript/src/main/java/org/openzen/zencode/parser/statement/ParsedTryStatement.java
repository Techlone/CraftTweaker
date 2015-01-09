/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openzen.zencode.parser.statement;

import java.util.ArrayList;
import java.util.List;
import org.openzen.zencode.lexer.Token;
import org.openzen.zencode.lexer.ZenLexer;
import static org.openzen.zencode.lexer.ZenLexer.*;
import org.openzen.zencode.parser.expression.ParsedExpression;
import org.openzen.zencode.parser.type.IParsedType;
import org.openzen.zencode.parser.type.TypeParser;
import org.openzen.zencode.symbolic.expression.IPartialExpression;
import org.openzen.zencode.symbolic.scope.IMethodScope;
import org.openzen.zencode.symbolic.statement.Statement;
import org.openzen.zencode.symbolic.statement.StatementSwitch;
import org.openzen.zencode.symbolic.statement.StatementVar;
import org.openzen.zencode.symbolic.statement.TryStatement;
import org.openzen.zencode.symbolic.type.IZenType;
import org.openzen.zencode.util.CodePosition;

/**
 *
 * @author Stan
 */
public class ParsedTryStatement extends ParsedStatement
{
	public static ParsedTryStatement parse(ZenLexer lexer)
	{
		CodePosition position = lexer.required(T_TRY, "try expected").getPosition();
		
		ParsedStatementVar withVariable = null;
		if (lexer.peek().getType() == TOKEN_ID) {
			Token variableName = lexer.next();
			lexer.required(T_ASSIGN, "= expected");
			ParsedExpression value = ParsedExpression.parse(lexer);
			withVariable = new ParsedStatementVar(
					variableName.getPosition(),
					variableName.getValue(),
					null,
					value,
					true);
		}
		
		ParsedStatement contents = ParsedStatement.parse(lexer);
		List<CatchClause> catches = new ArrayList<CatchClause>();
		while (lexer.peek().getType() == T_CATCH) {
			CodePosition catchPosition = lexer.next().getPosition();
			String name = lexer.requiredIdentifier();
			lexer.required(T_AS, "as required");
			
			List<IParsedType> types = new ArrayList<IParsedType>();
			do {
				types.add(TypeParser.parse(lexer));
			} while (lexer.optional(T_COMMA) != null);
			
			ParsedStatement catchContents = ParsedStatement.parse(lexer);
			catches.add(new CatchClause(catchPosition, name, types, catchContents));
		}
		
		ParsedStatement finallyContents = null;
		if (lexer.optional(T_FINALLY) != null)
			finallyContents = ParsedStatement.parse(lexer);
		
		return new ParsedTryStatement(
				position,
				withVariable,
				contents,
				catches,
				finallyContents);
	}
	
	private final ParsedStatementVar withVariable;
	private final ParsedStatement contents;
	private final List<CatchClause> catches;
	private final ParsedStatement finallyContents;
	
	public ParsedTryStatement(
			CodePosition position,
			ParsedStatementVar withVariable, 
			ParsedStatement contents,
			List<CatchClause> catches,
			ParsedStatement finallyContents)
	{
		super(position);
		
		this.withVariable = withVariable;
		this.contents = contents;
		this.catches = catches;
		this.finallyContents = finallyContents;
	}
	
	@Override
	public <E extends IPartialExpression<E, T>, T extends IZenType<E, T>> Statement<E, T> compile(IMethodScope<E, T> scope)
	{
		StatementVar<E, T> withVariableCompiled = null;
		if (withVariable != null)
			withVariableCompiled = withVariable.compile(scope);
		
		Statement<E, T> contentsCompiled = contents.compile(scope);
		List<TryStatement.CatchClause<E, T>> catchesCompiled = new ArrayList<TryStatement.CatchClause<E, T>>();
		for (CatchClause catchClause : catches) {
			catchesCompiled.add(catchClause.compile(scope));
		}
		
		Statement<E, T> finallyCompiled = finallyContents == null ? null : finallyContents.compile(scope);
		return new TryStatement<E, T>(
				getPosition(),
				scope,
				withVariableCompiled,
				contentsCompiled,
				catchesCompiled,
				finallyCompiled);
	}

	@Override
	public <E extends IPartialExpression<E, T>, T extends IZenType<E, T>> void compileSwitch(IMethodScope<E, T> scope, StatementSwitch<E, T> forSwitch)
	{
		forSwitch.onStatement(compile(scope));
	}
	
	public static class CatchClause
	{
		private CodePosition position;
		private String name;
		private List<IParsedType> types;
		private ParsedStatement contents;
		
		public CatchClause(
				CodePosition position, 
				String name,
				List<IParsedType> types,
				ParsedStatement contents)
		{
			this.position = position;
			this.name = name;
			this.types = types;
			this.contents = contents;
		}
		
		public <E extends IPartialExpression<E, T>, T extends IZenType<E, T>> TryStatement.CatchClause<E, T>
			compile(IMethodScope<E, T> scope)
		{
			List<T> compiledTypes = new ArrayList<T>();
			for (IParsedType type : types)
			{
				compiledTypes.add(type.compile(scope));
			}
			
			return new TryStatement.CatchClause<E, T>(compiledTypes, contents.compile(scope));
		}
	}
}
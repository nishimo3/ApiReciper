package org.pairprogrammingai.apireciper.core.analyzer.parser;

import org.pairprogrammingai.apireciper.core.data.SequenceInfo;
import java.util.List;

public interface BaseAstParser {
	public List<SequenceInfo> execute(String name, int id) throws Exception;
}

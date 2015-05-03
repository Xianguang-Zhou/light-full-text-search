/*
 * Copyright (C) 2015 Xianguang Zhou <xianguang.zhou@outlook.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.zxg.text.search;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextIndex {

	private Map<String, Set<String>> wordToDocIds;
	private Map<String, Set<String>> docIdToWords;

	public TextIndex() {
		wordToDocIds = new HashMap<>();
		docIdToWords = new HashMap<>();
	}

	private Set<String> separateWord(String content) {
		content = content.replace('.', ' ');
		content = content.replace(',', ' ');
		content = content.replace('!', ' ');
		content = content.replace('"', ' ');
		content = content.replace('\'', ' ');
		content = content.replace(':', ' ');
		content = content.replace(';', ' ');
		content = content.replace('?', ' ');

		String[] wordsArray = content.split(" ");

		Set<String> wordsSet = new HashSet<>();
		for (String word : wordsArray) {
			if (!word.isEmpty()) {
				wordsSet.add(word);
			}
		}
		return wordsSet;
	}

	private void addDocIdToWord(String word, String docId) {
		Set<String> docIds = wordToDocIds.get(word);
		if (docIds == null) {
			docIds = new HashSet<>();
			wordToDocIds.put(word, docIds);
		}
		docIds.add(docId);
	}

	private void addWordToDocId(String docId, String word) {
		Set<String> words = docIdToWords.get(docId);
		if (words == null) {
			words = new HashSet<>();
			docIdToWords.put(docId, words);
		}
		words.add(word);
	}

	public void addIndex(String id, String content) {
		Set<String> words = separateWord(content);
		for (String word : words) {
			addDocIdToWord(word, id);
			addWordToDocId(id, word);
		}
	}

	public void removeIndex(String id) {
		Set<String> words = docIdToWords.get(id);
		if (words != null) {
			for (String word : words) {
				Set<String> docIds = wordToDocIds.get(word);
				docIds.remove(id);
				if (docIds.isEmpty()) {
					wordToDocIds.remove(word);
				}
			}
			docIdToWords.remove(id);
		}
	}

	public Set<String> search(String query) {
		Set<String> words = separateWord(query);
		Set<String> docIds = new HashSet<>();
		for (String word : words) {
			Set<String> wordDocIds = wordToDocIds.get(word);
			if (wordDocIds != null) {
				docIds.addAll(wordDocIds);
			}
		}
		return docIds;
	}
}

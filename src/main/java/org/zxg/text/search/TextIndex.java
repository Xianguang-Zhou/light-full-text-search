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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TextIndex {

	private Map<String, Set<String>> wordToDocId;
	private Map<String, Map<String, Integer>> docIdToWordToFrequency;
	private Map<String, Integer> docIdToWordAmount;

	public TextIndex() {
		wordToDocId = new HashMap<>();
		docIdToWordToFrequency = new HashMap<>();
		docIdToWordAmount = new HashMap<>();
	}

	private String replacePunctuation(String text) {
		text = text.replace('.', ' ');
		text = text.replace(',', ' ');
		text = text.replace('!', ' ');
		text = text.replace('"', ' ');
		text = text.replace('\'', ' ');
		text = text.replace(':', ' ');
		text = text.replace(';', ' ');
		text = text.replace('?', ' ');
		return text;
	}

	private Set<String> separateWord(String text) {
		String[] wordsArray = replacePunctuation(text).split(" ");

		Set<String> wordsSet = new HashSet<>();
		for (String word : wordsArray) {
			if (!word.isEmpty()) {
				wordsSet.add(word);
			}
		}
		return wordsSet;
	}

	private Map<String, Integer> separateWordFrequency(String text) {
		String[] wordsArray = replacePunctuation(text).split(" ");

		Map<String, Integer> wordsFrequency = new HashMap<>();
		for (String word : wordsArray) {
			if (!word.isEmpty()) {
				Integer frequency = wordsFrequency.get(word);
				if (frequency == null) {
					frequency = 1;
				} else {
					++frequency;
				}
				wordsFrequency.put(word, frequency);
			}
		}
		return wordsFrequency;
	}

	private void addDocIdToWord(String word, String docId) {
		Set<String> docIds = wordToDocId.get(word);
		if (docIds == null) {
			docIds = new HashSet<>();
			wordToDocId.put(word, docIds);
		}
		docIds.add(docId);
	}

	public void addIndex(String id, String content) {
		Map<String, Integer> wordsFrequency = separateWordFrequency(content);
		if (!wordsFrequency.isEmpty()) {
			Integer wordAmount = 0;
			for (Map.Entry<String, Integer> entry : wordsFrequency.entrySet()) {
				addDocIdToWord(entry.getKey(), id);
				wordAmount += entry.getValue();
			}
			docIdToWordToFrequency.put(id, wordsFrequency);
			docIdToWordAmount.put(id, wordAmount);
		}
	}

	public void removeIndex(String id) {
		Map<String, Integer> wordsFrequency = docIdToWordToFrequency.get(id);
		if (wordsFrequency != null) {
			for (String word : wordsFrequency.keySet()) {
				Set<String> docIds = wordToDocId.get(word);
				docIds.remove(id);
				if (docIds.isEmpty()) {
					wordToDocId.remove(word);
				}
			}
			docIdToWordToFrequency.remove(id);
			docIdToWordAmount.remove(id);
		}
	}

	public List<String> search(String query) {
		Set<String> words = separateWord(query);

		Map<String, Double> docIdToScore = new HashMap<>();
		for (String word : words) {
			Set<String> wordDocIds = wordToDocId.get(word);
			if (wordDocIds != null) {
				for (String wordDocId : wordDocIds) {
					double termFrequency = ((double) docIdToWordToFrequency
							.get(wordDocId).get(word))
							/ docIdToWordAmount.get(wordDocId);
					double inverseDocumentFrequency = Math
							.log10(((double) docIdToWordToFrequency.size())
									/ wordDocIds.size());
					double weight = termFrequency * inverseDocumentFrequency;

					Double score = docIdToScore.get(wordDocId);
					if (score == null) {
						score = 0.0;
					}
					score += weight;
					docIdToScore.put(wordDocId, score);
				}
			}
		}

		List<DocIdAndScore> docIdAndScoreList = new ArrayList<DocIdAndScore>(
				docIdToScore.size());
		for (Map.Entry<String, Double> entry : docIdToScore.entrySet()) {
			docIdAndScoreList.add(new DocIdAndScore(entry.getKey(), entry
					.getValue()));
		}
		Collections.sort(docIdAndScoreList, new Comparator<DocIdAndScore>() {
			@Override
			public int compare(DocIdAndScore o1, DocIdAndScore o2) {
				double score = o2.score - o1.score;
				if (score > 0) {
					return 1;
				} else if (score < 0) {
					return -1;
				} else {
					return 0;
				}
			}
		});

		List<String> docIdList = new ArrayList<>(docIdAndScoreList.size());
		for (DocIdAndScore docIdAndScore : docIdAndScoreList) {
			docIdList.add(docIdAndScore.docId);
		}
		return docIdList;
	}

	private static class DocIdAndScore {
		public String docId;
		public Double score;

		public DocIdAndScore(String docId, Double score) {
			this.docId = docId;
			this.score = score;
		}
	}
}

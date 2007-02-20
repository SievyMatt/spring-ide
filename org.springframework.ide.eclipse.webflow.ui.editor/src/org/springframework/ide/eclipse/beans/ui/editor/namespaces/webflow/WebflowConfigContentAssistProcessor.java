/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.ide.eclipse.beans.ui.editor.namespaces.webflow;

import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.springframework.ide.eclipse.beans.core.model.IBean;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.AbstractContentAssistProcessor;
import org.springframework.ide.eclipse.beans.ui.editor.contentassist.requestor.BeanReferenceSearchRequestor;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansEditorUtils;
import org.springframework.ide.eclipse.beans.ui.editor.util.BeansJavaCompletionUtils;
import org.w3c.dom.Node;

@SuppressWarnings("restriction")
public class WebflowConfigContentAssistProcessor extends AbstractContentAssistProcessor {

	private void addBeanReferenceProposals(ContentAssistRequest request, String prefix, Node node, boolean showExternal) {
		if (prefix == null) {
			prefix = "";
		}

		IFile file = (IFile) BeansEditorUtils.getResource(request);
		if (node.getOwnerDocument() != null) {
			BeanReferenceSearchRequestor requestor = new BeanReferenceSearchRequestor(request, BeansJavaCompletionUtils
					.getPropertyTypes(node, file.getProject()));
			Map<String, Node> beanNodes = BeansEditorUtils.getReferenceableNodes(node.getOwnerDocument());
			for (Map.Entry<String, Node> n : beanNodes.entrySet()) {
				Node beanNode = n.getValue();
				requestor.acceptSearchMatch(n.getKey(), beanNode, file, prefix);
			}
			if (showExternal) {
				List<?> beans = BeansEditorUtils.getBeansFromConfigSets(file);
				for (int i = 0; i < beans.size(); i++) {
					IBean bean = (IBean) beans.get(i);
					requestor.acceptSearchMatch(bean, file, prefix);
				}
			}
		}
	}

	private void addClassAttributeValueProposals(ContentAssistRequest request, String prefix) {
		BeansJavaCompletionUtils.addClassValueProposals(request, prefix);
	}

	@Override
	protected void computeAttributeNameProposals(ContentAssistRequest request, String prefix, String namespace,
			String namespacePrefix, Node attributeNode) {
	}

	@Override
	protected void computeAttributeValueProposals(ContentAssistRequest request, IDOMNode node, String matchString,
			String attributeName) {
		String nodeName = node.getNodeName();
		String prefix = node.getPrefix();
		if (prefix != null) {
			nodeName = nodeName.substring(prefix.length() + 1);
		}

		if ("executor".equals(nodeName)) {
			if ("registry-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("repository".equals(nodeName)) {
			if ("conversation-manager-ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("listener".equals(nodeName)) {
			if ("ref".equals(attributeName)) {
				addBeanReferenceProposals(request, matchString, node, true);
			}
		}
		else if ("attribute".equals(nodeName)) {
			if ("type".equals(attributeName)) {
				addClassAttributeValueProposals(request, matchString);
			}
		}
	}

	@Override
	protected void computeTagInsertionProposals(ContentAssistRequest request, IDOMNode node) {
	}
}

package com.sidebeam.bookmark.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sidebeam.bookmark.domain.model.PackageNode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * PackageNode의 커스텀 역직렬화기입니다.
 * 문자열 경로와 객체 형태의 패키지 데이터를 모두 처리할 수 있습니다.
 */
public class PackageNodeDeserializer extends JsonDeserializer<List<PackageNode>> {

    @Override
    public List<PackageNode> deserialize(JsonParser parser, DeserializationContext context)
            throws IOException {

        JsonNode node = parser.getCodec().readTree(parser);

        if (node == null || !node.isArray()) {
            return new ArrayList<>();
        }

        List<String> packagePaths = new ArrayList<>();
        List<PackageNode> packageNodes = new ArrayList<>();
        boolean hasStringPaths = false;
        boolean hasObjectPaths = false;

        // 배열의 각 요소를 검사
        for (JsonNode item : node) {
            if (item.isTextual()) {
                // 문자열 경로인 경우
                packagePaths.add(item.asText());
                hasStringPaths = true;
            } else if (item.isObject() && item.has("key")) {
                // 객체 형태인 경우
                PackageNode packageNode = parsePackageNodeObject(item);
                packageNodes.add(packageNode);
                hasObjectPaths = true;
            }
        }

        // 혼재된 형태는 지원하지 않음
        if (hasStringPaths && hasObjectPaths) {
            throw new IOException("Mixed package formats are not supported. Use either string paths or object format consistently.");
        }

        if (hasStringPaths) {
            // 문자열 경로를 트리로 변환
            PackageNode root = PackageTreeBuilder.buildTree(packagePaths);
            return root.getChildren() != null ? root.getChildren() : new ArrayList<>();
        } else {
            // 이미 객체 형태
            return packageNodes;
        }
    }

    private PackageNode parsePackageNodeObject(JsonNode node) throws IOException {
        String key = node.get("key").asText();
        List<PackageNode> children = new ArrayList<>();

        JsonNode childrenNode = node.get("children");
        if (childrenNode != null && childrenNode.isArray()) {
            for (JsonNode childNode : childrenNode) {
                children.add(parsePackageNodeObject(childNode));
            }
        }

        return PackageNode.builder()
                .key(key)
                .children(children.isEmpty() ? null : children)
                .build();
    }

}

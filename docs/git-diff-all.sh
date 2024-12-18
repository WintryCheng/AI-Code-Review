#!/bin/bash
# 获取最近一次git提交信息和提交内容，并拼接为json，保存为本地文件后通过post请求发送到远程服务器

# 1. 获取提交信息（已确保为有效 JSON）
commitInfo=$(git log -1 --pretty=format:"{\"commit\":\"%H\",\"author\":\"%an\",\"date\":\"%ad\",\"message\":\"%s\"}" --date=iso)
# 可选：将提交信息输出到控制台
printf "commitInfo: %s\n" "$commitInfo"

# 2. 使用 git show 获取最近一次提交的具体修改内容
isFirstCommit=$(git rev-list --count HEAD)
if [ "$isFirstCommit" -eq 1 ]; then
    # 首次提交，使用 git show 获取所有变更内容
    diffInfo=$(git show --format=format: | jq -R . | jq -s 'join("\n")')
else
    # 后续提交，使用 git show 获取最近一次提交的具体修改内容
    diffInfo=$(git show HEAD --format=format: | jq -R . | jq -s 'join("\n")')
fi
# 可选：将提交信息输出到控制台
printf "diffInfo: %s\n" "$diffInfo"

# 3. 将两者合并为单个 JSON 对象
jsonPayload=$(printf '{"commitInfo":%s,"diffInfo":%s}' "$commitInfo" "$diffInfo")
# 可选：将拼接json输出到控制台
printf "jsonPayload: %s\n" "$jsonPayload"

# 4. 将 JSON 对象保存到文件
author=$(echo "$commitInfo" | jq -r '.author' | sed 's/[^a-zA-Z0-9]/_/g') # 提取 author 并清理不适合用作文件名的字符
date=$(echo "$commitInfo" | jq -r '.date' | sed 's/[^a-zA-Z0-9]/_/g') # 提取 date 并清理不适合用作文件名的字符
jsonFileName="${author}_${date}.json"
printf "%s" "$jsonPayload" > "${jsonFileName}"

# 5. 发送 POST 请求
curl -X POST http://localhost:8081/ai/review/diffJson \
     -H "Content-Type: application/json" \
     --data-binary "@${jsonFileName}" \
     -o /dev/null \
     -w "请求已发送" \
     -s &
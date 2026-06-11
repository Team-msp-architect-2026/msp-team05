import boto3
import json
import logging
import os
import urllib.request

logger = logging.getLogger()
logger.setLevel(logging.INFO)

WAF_ACL_ID = os.environ["WAF_ACL_ID"]
WAF_ACL_ARN = os.environ["WAF_ACL_ARN"]
WAF_ACL_NAME = os.environ["WAF_ACL_NAME"]
WAF_SCOPE = os.environ.get("WAF_SCOPE", "CLOUDFRONT")
WAF_REGION = os.environ.get("WAF_REGION", "us-east-1")
SLACK_WEBHOOK_URL = os.environ.get("SLACK_WEBHOOK_URL", "")

RATE_RULE_NAME = "RateBasedRule"
RATE_LIMIT_NORMAL = 1000
RATE_LIMIT_BLOCKED = 100

WAF_ACL_ID        = os.environ["WAF_ACL_ID"]
WAF_ACL_ARN       = os.environ["WAF_ACL_ARN"]
WAF_ACL_NAME      = os.environ["WAF_ACL_NAME"]
WAF_SCOPE         = os.environ.get("WAF_SCOPE", "CLOUDFRONT")
WAF_REGION        = os.environ.get("WAF_REGION", "us-east-1")
SLACK_WEBHOOK_URL = os.environ.get("SLACK_WEBHOOK_URL", "")

RATE_RULE_NAME     = "RateBasedRule"
RATE_LIMIT_NORMAL  = 1000
RATE_LIMIT_BLOCKED = 100


def lambda_handler(event, context):
    logger.info("Event received: %s", json.dumps(event))
    for record in event.get("Records", []):
        raw_message = record["Sns"]["Message"]
        message     = json.loads(raw_message)
        alarm_name  = message.get("AlarmName", "unknown")
        alarm_state = message.get("NewStateValue", "")
        reason      = message.get("NewStateReason", "")
        logger.info("Alarm=%s State=%s", alarm_name, alarm_state)

        if alarm_state == "ALARM":
            _update_rate_limit(RATE_LIMIT_BLOCKED)
            _send_slack(
                f":red_circle: *WAF 경보 발생*\n"
                f"• 알람: `{alarm_name}`\n"
                f"• 상태: `ALARM`\n"
                f"• 조치: Rate Limit {RATE_LIMIT_NORMAL} → {RATE_LIMIT_BLOCKED} 으로 강화\n"
                f"• 원인: {reason}"
            )
        elif alarm_state == "OK":
            _update_rate_limit(RATE_LIMIT_NORMAL)
            _send_slack(
                f":large_green_circle: *WAF 경보 해제*\n"
                f"• 알람: `{alarm_name}`\n"
                f"• 상태: `OK`\n"
                f"• 조치: Rate Limit {RATE_LIMIT_BLOCKED} → {RATE_LIMIT_NORMAL} 으로 복구"
            )
        else:
            logger.info("No action for state: %s", alarm_state)

    return {"statusCode": 200, "body": "done"}


def _send_slack(text: str):
    if not SLACK_WEBHOOK_URL:
        logger.warning("SLACK_WEBHOOK_URL not set, skipping")
        return
    payload = json.dumps({"text": text}).encode("utf-8")
    req = urllib.request.Request(
        SLACK_WEBHOOK_URL,
        data=payload,
        headers={"Content-Type": "application/json"},
        method="POST",
    )
    try:
        with urllib.request.urlopen(req, timeout=5) as resp:
            logger.info("Slack response: %s", resp.status)
    except Exception as e:
        logger.error("Slack 전송 실패: %s", e)


def _update_rate_limit(new_limit: int):
    waf = boto3.client("wafv2", region_name=WAF_REGION)
    response = waf.get_web_acl(Name=WAF_ACL_NAME, Scope=WAF_SCOPE, Id=WAF_ACL_ID)
    web_acl = response["WebACL"]

def _update_rate_limit(new_limit: int):
    waf = boto3.client("wafv2", region_name=WAF_REGION)
    response   = waf.get_web_acl(Name=WAF_ACL_NAME, Scope=WAF_SCOPE, Id=WAF_ACL_ID)
    web_acl    = response["WebACL"]
    lock_token = response["LockToken"]
    updated_rules = []
    for rule in web_acl["Rules"]:
        if rule["Name"] == RATE_RULE_NAME:
            current_limit = rule["Statement"]["RateBasedStatement"]["Limit"]
            rule["Statement"]["RateBasedStatement"]["Limit"] = new_limit
            logger.info("RateBasedRule limit: %d → %d", current_limit, new_limit)
        updated_rules.append(rule)
    waf.update_web_acl(
        Name=web_acl["Name"],
        Scope=WAF_SCOPE,
        Id=WAF_ACL_ID,
        DefaultAction=web_acl["DefaultAction"],
        Rules=updated_rules,
        VisibilityConfig=web_acl["VisibilityConfig"],
        LockToken=lock_token,
    )
    logger.info("WAF update complete. New rate limit: %d", new_limit)

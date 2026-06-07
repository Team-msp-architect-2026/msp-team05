import boto3
import json
import logging
import os

logger = logging.getLogger()
logger.setLevel(logging.INFO)

WAF_ACL_ID = os.environ["WAF_ACL_ID"]
WAF_ACL_ARN = os.environ["WAF_ACL_ARN"]
WAF_ACL_NAME = os.environ["WAF_ACL_NAME"]
WAF_SCOPE = os.environ.get("WAF_SCOPE", "CLOUDFRONT")
WAF_REGION = os.environ.get("WAF_REGION", "us-east-1")

RATE_RULE_NAME = "RateBasedRule"
RATE_LIMIT_NORMAL = 1000
RATE_LIMIT_BLOCKED = 100

def lambda_handler(event, context):
    logger.info("Event received: %s", json.dumps(event))
    for record in event.get("Records", []):
        raw_message = record["Sns"]["Message"]
        message = json.loads(raw_message)
        alarm_name = message.get("AlarmName", "unknown")
        alarm_state = message.get("NewStateValue", "")
        logger.info("Alarm=%s State=%s", alarm_name, alarm_state)
        if alarm_state == "ALARM":
            _update_rate_limit(RATE_LIMIT_BLOCKED)
        elif alarm_state == "OK":
            _update_rate_limit(RATE_LIMIT_NORMAL)
        else:
            logger.info("No action for state: %s", alarm_state)
    return {"statusCode": 200, "body": "done"}

def _update_rate_limit(new_limit: int):
    waf = boto3.client("wafv2", region_name=WAF_REGION)
    response = waf.get_web_acl(Name=WAF_ACL_NAME, Scope=WAF_SCOPE, Id=WAF_ACL_ID)
    web_acl = response["WebACL"]
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
import json
import boto3

def lambda_handler(event, context):
    # Get an instance of EC2 client
    body = json.loads(event["body"])
    #I need to use 2 inorder to make this work as my EC2 instance is under 2
    region = body["region"]
    ec2 = boto3.client("ec2", region_name=region)

    # Call to describe instances
    instances = ec2.describe_instances()

    # Analyze the response to extract the public IP addresses of instances
    result = ""

    if 'Reservations' in instances:
        for reservation in instances['Reservations']:
            for instance in reservation.get('Instances', []):
                if 'PublicIpAddress' in instance:
                    if result:
                        result += ", "
                    result += instance['PublicIpAddress']

    # Return the result
    return result